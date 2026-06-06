/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;
import static io.jikkou.core.ReconciliationMode.UPDATE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.Configs;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selector;
import io.jikkou.kafka.ApiVersions;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.change.group.UpdateGroupConfigsHandler;
import io.jikkou.kafka.change.share.DeleteShareGroupHandler;
import io.jikkou.kafka.change.share.ShareGroupChangeComputer;
import io.jikkou.kafka.change.share.ShareGroupChangeDescription;
import io.jikkou.kafka.internals.admin.AdminClientContext;
import io.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.models.V1KafkaShareGroupSpec;
import io.jikkou.kafka.reconciler.service.KafkaAdminService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Kafka share groups")
@Description("Reconciles Kafka share group resources to ensure they match the desired state.")
@SupportedResource(type = V1KafkaShareGroup.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1, kind = "KafkaShareGroupChange")
@ControllerConfiguration(supportedModes = {CREATE, UPDATE, FULL, DELETE})
public final class AdminClientShareGroupController
        extends ContextualExtension implements Controller<V1KafkaShareGroup> {

    /**
     * The Extension config.
     */
    public interface Config {
        ConfigProperty<Boolean> IS_CONFIG_DELETE_ORPHANS_ENABLED = ConfigProperty
            .ofBoolean("config-delete-orphans")
            .displayName("Delete Orphan Configs")
            .description("Specify whether to delete group configs that exist on the cluster but are not defined in the resource.")
            .defaultValue(true);
    }

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientShareGroupController} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientShareGroupController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientShareGroupController} instance with the specified {@link AdminClientContextFactory}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use.
     */
    public AdminClientShareGroupController(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (adminClientContextFactory == null) {
            adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler> handlers = List.of(
                new UpdateGroupConfigsHandler(adminClient),
                new DeleteShareGroupHandler(adminClient),
                new ChangeHandler.None(ShareGroupChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceChange> plan(@NotNull Collection<V1KafkaShareGroup> resources,
                                     @NotNull ReconciliationContext context) {
        Selector selector = context.selector();

        // Desired state with flattened configs.
        List<V1KafkaShareGroup> expected = resources.stream()
            .filter(selector::apply)
            .map(resource -> resource.withStatus(null))
            .map(resource -> {
                V1KafkaShareGroupSpec spec = resource.getSpec();
                Configs configs = spec == null ? null : spec.getConfigs();
                return configs == null ? resource : resource.withSpec(spec.withConfigs(configs.flatten()));
            })
            .toList();

        List<String> ids = expected.stream()
            .map(resource -> resource.getMetadata().getName())
            .distinct()
            .toList();

        // Actual state is derived from the dynamic configs of the GROUP resource (which never
        // fails for not-yet-existing groups), not from listing share groups (which omits
        // config-only groups).
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            KafkaAdminService service = new KafkaAdminService(clientContext.getAdminClient());
            Map<String, Configs> actualConfigs = service.describeGroupConfigs(ids);

            List<V1KafkaShareGroup> actual = ids.stream()
                .map(id -> V1KafkaShareGroup.builder()
                    .withMetadata(ObjectMeta.builder().withName(id).build())
                    .withSpec(V1KafkaShareGroupSpec.builder()
                        .withConfigs(actualConfigs.getOrDefault(id, Configs.empty()))
                        .build())
                    .build())
                .toList();

            ShareGroupChangeComputer computer = new ShareGroupChangeComputer(
                Config.IS_CONFIG_DELETE_ORPHANS_ENABLED.get(context.configuration()));
            return computer.computeChanges(actual, expected);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(Config.IS_CONFIG_DELETE_ORPHANS_ENABLED);
    }
}
