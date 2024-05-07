/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.ApiVersions;
import io.streamthoughts.jikkou.kafka.change.topics.CreateTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.change.topics.DeleteTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChange;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.change.topics.UpdateTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTopic.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA2, kind = "KafkaTopicChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaTopicController
        extends ContextualExtension implements Controller<V1KafkaTopic, ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicController.class);

    public static final String CONFIG_ENTRY_DELETE_ORPHANS_CONFIG_NAME = "config-delete-orphans";

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientKafkaTopicController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaTopicController(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(context.appConfiguration());
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull final ReconciliationContext context) {

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                    new CreateTopicChangeHandler(adminClient),
                    new UpdateTopicChangeHandler(adminClient),
                    new DeleteTopicChangeHandler(adminClient),
                    new ChangeHandler.None<>(TopicChange::getDescription)
            );
            return executor.applyChanges(handlers);
        }
    }

    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1KafkaTopic> resources,
            @NotNull ReconciliationContext context) {

        LOG.info("Computing reconciliation change for '{}' resources", resources.size());

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaTopic> expectedKafkaTopics = resources.stream()
                .filter(context.selector()::apply)
                .map(resource -> {
                    V1KafkaTopicSpec spec = resource.getSpec();
                    Configs configs = spec.getConfigs();
                    return configs == null ? resource : resource.withSpec(spec.withConfigs(configs.flatten()));
                })
                .toList();

        // Build the configuration for describing actual resources
        Configuration configuration = Configuration.from(Map.of(
                WithKafkaConfigFilters.DEFAULT_CONFIGS_CONFIG, true,
                WithKafkaConfigFilters.DYNAMIC_BROKER_CONFIGS_CONFIG, true,
                WithKafkaConfigFilters.STATIC_BROKER_CONFIGS_CONFIG, true
        ));

        // Get the list of remote resources that are candidates for this reconciliation
        AdminClientKafkaTopicCollector collector = new AdminClientKafkaTopicCollector(adminClientContextFactory);
        collector.init(extensionContext().contextForExtension(AdminClientKafkaTopicCollector.class));

        List<V1KafkaTopic> actualKafkaTopics = collector.listAll(configuration, Selectors.NO_SELECTOR)
                .stream()
                .filter(context.selector()::apply)
                .toList();

        boolean isConfigDeletionEnabled = isConfigDeletionEnabled(context);

        TopicChangeComputer changeComputer = new TopicChangeComputer(isConfigDeletionEnabled);
        return changeComputer.computeChanges(actualKafkaTopics, expectedKafkaTopics);
    }

    @VisibleForTesting
    static boolean isConfigDeletionEnabled(@NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(CONFIG_ENTRY_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(true)
                .get(context.configuration());
    }
}
