/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.*;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.ApiVersions;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.change.topics.*;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTopic.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA2, kind = "KafkaTopicChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaTopicController
        extends ContextualExtension implements Controller<V1KafkaTopic> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicController.class);

    /**
     * The Extension config
     */
    public interface Config {
        ConfigProperty<Boolean> IS_CONFIG_DELETE_ORPHANS_ENABLED = ConfigProperty
            .ofBoolean("config-delete-orphans")
            .defaultValue(true);

        ConfigProperty<Boolean> IS_DELETE_ORPHANS_ENABLED = ConfigProperty
            .ofBoolean("delete-orphans")
            .defaultValue(false);
    }


    private AdminClientContextFactory adminClientContextFactory;

    private List<Pattern> topicDeleteExcludePatterns;

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

        KafkaExtensionProvider provider = context.provider();

        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = provider.newAdminClientContextFactory();
        }
        this.topicDeleteExcludePatterns = provider.topicDeleteExcludePatterns();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull final ReconciliationContext context) {

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler> handlers = List.of(
                new CreateTopicChangeHandler(adminClient),
                new UpdateTopicChangeHandler(adminClient),
                new DeleteTopicChangeHandler(adminClient),
                new ChangeHandler.None(TopicChange::getDescription)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1KafkaTopic> resources,
            @NotNull ReconciliationContext context) {

        LOG.info("Computing reconciliation change for '{}' resources", resources.size());

        Selector selector = context.selector();
        boolean deleteOrphans = Config.IS_DELETE_ORPHANS_ENABLED.get(context.configuration());

        if (deleteOrphans && Selectors.containsLabelSelector(selector)) {
            throw new JikkouRuntimeException(
                "Cannot use label selectors together with delete-orphans=true. " +
                "Label selectors filter out actual resources that lack user-defined labels, " +
                "which would cause all non-matching topics to be deleted."
            );
        }

        // Get the list of expected resources with flattened configs (unfiltered)
        List<V1KafkaTopic> allExpectedKafkaTopics = resources.stream()
                .map(resource -> {
                    V1KafkaTopicSpec spec = resource.getSpec();
                    Configs configs = spec.getConfigs();
                    return configs == null ? resource : resource.withSpec(spec.withConfigs(configs.flatten()));
                })
                .toList();

        // Get the list of actual remote resources (unfiltered)
        AdminClientKafkaTopicCollector collector = new AdminClientKafkaTopicCollector(adminClientContextFactory);
        collector.init(extensionContext().contextForExtension(AdminClientKafkaTopicCollector.class));
        List<V1KafkaTopic> allActualKafkaTopics = collector.listAll().getItems();

        // Enrich actual topics with labels from expected topics so label selectors work on both sides
        enrichLabelsFromExpected(allActualKafkaTopics, allExpectedKafkaTopics);

        // Now apply the selector to both sides
        List<V1KafkaTopic> expectedKafkaTopics = allExpectedKafkaTopics.stream()
                .filter(selector::apply)
                .toList();

        List<V1KafkaTopic> actualKafkaTopics = allActualKafkaTopics.stream()
                .filter(selector::apply)
                .toList();

        TopicChangeComputer changeComputer = new TopicChangeComputer(
            topicDeleteExcludePatterns,
            deleteOrphans,
            Config.IS_CONFIG_DELETE_ORPHANS_ENABLED.get(context.configuration())
        );
        return changeComputer.computeChanges(actualKafkaTopics, expectedKafkaTopics);
    }

    /**
     * Enriches actual topics with labels from matching expected topics.
     * Labels from expected topics are propagated to actual topics (joined by name),
     * preserving any existing labels on the actual topics (e.g., system labels).
     *
     * @param actual   the list of actual (collected) topics.
     * @param expected the list of expected (input) topics.
     */
    static void enrichLabelsFromExpected(
            @NotNull List<V1KafkaTopic> actual,
            @NotNull List<V1KafkaTopic> expected) {

        Map<String, Map<String, Object>> labelsByName = expected.stream()
                .collect(Collectors.toMap(
                        t -> t.getMetadata().getName(),
                        t -> t.getMetadata().getLabels(),
                        (a, b) -> b // last-one-wins for duplicate names
                ));

        for (V1KafkaTopic topic : actual) {
            Map<String, Object> expectedLabels = labelsByName.get(topic.getMetadata().getName());
            if (expectedLabels == null || expectedLabels.isEmpty()) {
                continue;
            }
            expectedLabels.forEach(topic.getMetadata()::addLabelIfAbsent);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.IS_DELETE_ORPHANS_ENABLED,
            Config.IS_CONFIG_DELETE_ORPHANS_ENABLED
        );
    }
}
