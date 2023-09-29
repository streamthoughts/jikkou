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
package io.streamthoughts.jikkou.kafka.control;

import static io.streamthoughts.jikkou.api.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.api.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.UPDATE;

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
import io.streamthoughts.jikkou.kafka.change.TopicChange;
import io.streamthoughts.jikkou.kafka.change.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.topics.CreateTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.topics.DeleteTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.topics.TopicChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.topics.UpdateTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaTopicListConverter;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaTopic.class)
@AcceptsResource(type = V1KafkaTopicList.class, converter = V1KafkaTopicListConverter.class)
@AcceptsReconciliationModes({CREATE, DELETE, UPDATE, APPLY_ALL})
public final class AdminClientKafkaTopicController
        implements BaseResourceController<V1KafkaTopic, TopicChange> {

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
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        LOG.info("Configuring");
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(configuration);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<TopicChange>> execute(@NotNull List<HasMetadataChange<TopicChange>> changes,
                                                   @NotNull ReconciliationMode mode,
                                                   boolean dryRun) {

        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = context.getAdminClient();
            List<ChangeHandler<TopicChange>> handlers = List.of(
                    new CreateTopicChangeHandler(adminClient),
                    new UpdateTopicChangeHandler(adminClient),
                    new DeleteTopicChangeHandler(adminClient),
                    new ChangeHandler.None<>(TopicChangeDescription::new)
            );
            return new ChangeExecutor<>(handlers).execute(changes, dryRun);
        }
    }

    @Override
    public V1KafkaTopicChangeList computeReconciliationChanges(
            @NotNull Collection<V1KafkaTopic> resources,
            @NotNull ReconciliationMode mode, @NotNull
            ReconciliationContext context) {

        LOG.info("Computing reconciliation change for '{}' resources in '{}' mode", resources.size(), mode);

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaTopic> expectedKafkaTopics = resources.stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Build the configuration for describing actual resources
        Configuration describeConfiguration = new ConfigDescribeConfiguration()
                .withDescribeDefaultConfigs(true)
                .withDescribeStaticBrokerConfigs(true)
                .withDescribeDynamicBrokerConfigs(true)
                .asConfiguration();

        // Get the list of remote resources that are candidates for this reconciliation
        AdminClientKafkaTopicCollector collector = new AdminClientKafkaTopicCollector(adminClientContextFactory);
        List<V1KafkaTopic> actualKafkaTopics = collector.listAll(describeConfiguration)
                .stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        boolean isConfigDeletionEnabled = isConfigDeletionEnabled(mode, context);

        TopicChangeComputer changeComputer = new TopicChangeComputer(isConfigDeletionEnabled);

        // Compute changes
        List<V1KafkaTopicChange> changes = changeComputer.computeChanges(actualKafkaTopics, expectedKafkaTopics)
                .stream()
                .map(it -> V1KafkaTopicChange.builder()
                        .withMetadata(it.getMetadata())
                        .withChange(it.getChange())
                        .build()
                ).collect(Collectors.toList());
        return V1KafkaTopicChangeList.builder().withItems(changes).build();
    }

    @VisibleForTesting
    static boolean isConfigDeletionEnabled(@NotNull ReconciliationMode mode, @NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(CONFIG_ENTRY_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(() -> List.of(APPLY_ALL, DELETE, UPDATE).contains(mode))
                .evaluate(context.configuration());
    }
}
