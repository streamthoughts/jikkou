/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;

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
import io.streamthoughts.jikkou.kafka.change.acl.AclChangeComputer;
import io.streamthoughts.jikkou.kafka.change.acl.CreateAclChangeHandler;
import io.streamthoughts.jikkou.kafka.change.acl.DeleteAclChangeHandler;
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
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, FULL}
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
            this.adminClientContextFactory = new AdminClientContextFactory(context.appConfiguration());
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
                    new CreateAclChangeHandler(adminClient),
                    new DeleteAclChangeHandler(adminClient),
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
