/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.control;

import static io.streamthoughts.jikkou.api.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.api.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.DELETE;

import io.streamthoughts.jikkou.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.ChangeExecutor;
import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseResourceController;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.kafka.change.AclChange;
import io.streamthoughts.jikkou.kafka.change.AclChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.AclChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.CreateAclChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.DeleteAclChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.builder.LiteralKafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.change.handlers.acls.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAclChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAclChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaPrincipalAuthorization.class)
@AcceptsReconciliationModes(value = {CREATE, DELETE, APPLY_ALL})
public final class AdminClientKafkaAclController
        implements BaseResourceController<V1KafkaPrincipalAuthorization, AclChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaAclController.class);

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
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        LOG.info("Configuring");
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(configuration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaAclChangeList computeReconciliationChanges(@NotNull Collection<V1KafkaPrincipalAuthorization> resources,
                                                             @NotNull ReconciliationMode mode,
                                                             @NotNull ReconciliationContext context) {

        // Get the list of remote resources that are candidates for this reconciliation
        List<V1KafkaPrincipalAuthorization> expectedStates = resources
                .stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {

            final AdminClient adminClient = clientContext.getAdminClient();

            // Get the actual state from the cluster.
            AdminClientKafkaAclCollector collector = new AdminClientKafkaAclCollector(adminClientContextFactory);
            List<V1KafkaPrincipalAuthorization> actualStates = collector.listAll(adminClient)
                    .stream()
                    .filter(new AggregateSelector(context.selectors())::apply)
                    .toList();

            // Compute state changes
            final KafkaAclBindingBuilder builder = KafkaAclBindingBuilder.combines(
                    new LiteralKafkaAclBindingBuilder(),
                    new TopicMatchingAclRulesBuilder(adminClient)
            );

            AclChangeComputer computer = new AclChangeComputer(
                    new Config(context.configuration()).isDeleteOrphansEnabled(),
                    builder);

            List<V1KafkaAclChange> changes = computer.computeChanges(actualStates, expectedStates)
                    .stream()
                    .map(it -> V1KafkaAclChange.builder()
                            .withMetadata(it.getMetadata())
                            .withChange(it.getChange())
                            .build()
                    ).collect(Collectors.toList());

            return new V1KafkaAclChangeList().withItems(changes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<AclChange>> execute(@NotNull List<HasMetadataChange<AclChange>> changes,
                                                 @NotNull ReconciliationMode mode,
                                                 boolean dryRun) {
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = context.getAdminClient();
            List<ChangeHandler<AclChange>> handlers = List.of(
                    new CreateAclChangeHandler(adminClient),
                    new DeleteAclChangeHandler(adminClient),
                    new ChangeHandler.None<>(AclChangeDescription::new)
            );
            return new ChangeExecutor<>(handlers).execute(changes, dryRun);
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
            return DELETE_ORPHANS_OPTIONS_CONFIG.evaluate(configuration);
        }
    }
}
