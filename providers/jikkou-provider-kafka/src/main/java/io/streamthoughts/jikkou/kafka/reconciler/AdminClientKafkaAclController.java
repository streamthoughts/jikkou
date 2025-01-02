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
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.kafka.ApiVersions;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.change.acl.AclChangeComputer;
import io.streamthoughts.jikkou.kafka.change.acl.AclChangeHandler;
import io.streamthoughts.jikkou.kafka.change.acl.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.change.acl.KafkaPrincipalAuthorizationDescription;
import io.streamthoughts.jikkou.kafka.change.acl.builder.LiteralKafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.change.acl.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaPrincipalAuthorization.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA2, kind = "KafkaPrincipalAuthorizationChange")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaAclController
    extends ContextualExtension
    implements Controller<V1KafkaPrincipalAuthorization, ResourceChange> {

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaAclController} instance.
     */
    public AdminClientKafkaAclController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAclController} instance.
     *
     * @param AdminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaAclController(final @NotNull AdminClientContextFactory AdminClientContextFactory) {
        this.adminClientContextFactory = AdminClientContextFactory;
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
    public List<ResourceChange> plan(@NotNull Collection<V1KafkaPrincipalAuthorization> resources,
                                     @NotNull ReconciliationContext context) {

        // Get the list of remote resources that are candidates for this reconciliation
        List<V1KafkaPrincipalAuthorization> expectedStates = resources
            .stream()
            .filter(context.selector()::apply)
            .toList();

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {

            final AdminClient adminClient = clientContext.getAdminClient();

            // Get the actual state from the cluster.
            AdminClientKafkaAclCollector collector = new AdminClientKafkaAclCollector(adminClientContextFactory);
            collector.init(extensionContext().contextForExtension(AdminClientKafkaAclCollector.class));

            List<V1KafkaPrincipalAuthorization> actualStates = collector.listAll(adminClient)
                .stream()
                .filter(context.selector()::apply)
                .toList();

            // Compute state changes
            final KafkaAclBindingBuilder builder = KafkaAclBindingBuilder.combines(
                new LiteralKafkaAclBindingBuilder(),
                new TopicMatchingAclRulesBuilder(adminClient)
            );

            AclChangeComputer computer = new AclChangeComputer(
                new Config(context.configuration()).isDeleteOrphansEnabled(),
                builder);

            return computer.computeChanges(actualStates, expectedStates);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor<ResourceChange> executor,
                                      @NotNull ReconciliationContext context) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                new AclChangeHandler(adminClient),
                new ChangeHandler.None<>(KafkaPrincipalAuthorizationDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    public static class Config {

        public static final ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS_CONFIG = ConfigProperty
            .ofBoolean("delete-orphans")
            .orElse(false);

        private final Configuration configuration;

        public Config(Configuration configuration) {
            this.configuration = configuration;
        }

        public boolean isDeleteOrphansEnabled() {
            return DELETE_ORPHANS_OPTIONS_CONFIG.get(configuration);
        }
    }
}
