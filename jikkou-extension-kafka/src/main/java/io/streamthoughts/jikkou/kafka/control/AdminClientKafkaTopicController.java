/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.annotations.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseResourceController;
import io.streamthoughts.jikkou.api.control.ChangeExecutor;
import io.streamthoughts.jikkou.api.control.ChangeHandler;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.streamthoughts.jikkou.kafka.control.change.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.control.handlers.topics.AlterTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.topics.CreateTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.topics.DeleteTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.topics.TopicChangeDescription;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaTopicListConverter;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaTopic.class)
@AcceptsResource(type = V1KafkaTopicList.class, converter = V1KafkaTopicListConverter.class)
@AcceptsReconciliationModes( { CREATE, DELETE, UPDATE, APPLY_ALL})
public final class AdminClientKafkaTopicController extends AbstractAdminClientKafkaController
        implements BaseResourceController<V1KafkaTopic, TopicChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicController.class);

    public static final String CONFIG_ENTRY_DELETE_ORPHANS_CONFIG_NAME = "config-delete-orphans";

    private AdminClientKafkaTopicCollector collector;

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance.
     */
    public AdminClientKafkaTopicController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaTopicController(final @NotNull Configuration config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaTopicController(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
        setInternalDescriptor(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        super.configure(config);
        setInternalDescriptor(adminClientContext);
    }

    private void setInternalDescriptor(@NotNull AdminClientContext adminClientContext) {
        if (collector == null) {
            this.collector = new AdminClientKafkaTopicCollector(adminClientContext);
        }
    }


    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<TopicChange>> execute(@NotNull List<TopicChange> changes,
                                                   @NotNull ReconciliationMode mode,
                                                   boolean dryRun) {
        AdminClient client = adminClientContext.client();
        List<ChangeHandler<TopicChange>> handlers = List.of(
                new CreateTopicChangeHandler(client),
                new AlterTopicChangeHandler(client),
                new DeleteTopicChangeHandler(client),
                new ChangeHandler.None<>(TopicChangeDescription::new)
        );
        return new ChangeExecutor<>(handlers).execute(changes, dryRun);
    }

    @Override
    public ResourceListObject<? extends HasMetadataChange<TopicChange>> computeReconciliationChanges(
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
        List<V1KafkaTopic> actualKafkaTopics = collector.listAll(describeConfiguration, context.selectors());


        boolean isConfigDeletionEnabled = isConfigDeletionEnabled(mode, context);

        TopicChangeComputer changeComputer = new TopicChangeComputer(isConfigDeletionEnabled);

        List<TopicChange> changes = changeComputer.computeChanges(
                actualKafkaTopics,
                expectedKafkaTopics);

        return new V1KafkaTopicChangeList()
                .withItems(changes.stream().map(c -> V1KafkaTopicChange.builder().withChange(c).build()).toList());
    }

    @VisibleForTesting
    static boolean isConfigDeletionEnabled(@NotNull ReconciliationMode mode, @NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(CONFIG_ENTRY_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(() -> List.of(APPLY_ALL, DELETE, UPDATE).contains(mode))
                .evaluate(context.configuration());
    }
}
