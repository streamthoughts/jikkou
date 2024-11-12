/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;
import static io.streamthoughts.jikkou.extension.aiven.ApiVersions.KAFKA_AIVEN_V1BETA2;

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
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.change.topic.KafkaTopicChangeHandler;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import io.streamthoughts.jikkou.kafka.reconciler.KafkaConfigsConfig;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(apiVersion = KAFKA_AIVEN_V1BETA2, kind = "KafkaTopicChange")
@SupportedResource(apiVersion = KAFKA_AIVEN_V1BETA2, kind = "KafkaTopic")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AivenKafkaTopicController
    extends ContextualExtension implements Controller<V1KafkaTopic, ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AivenKafkaTopicController.class);

    interface Config {
        ConfigProperty<Boolean> IS_CONFIG_DELETE_ORPHANS_ENABLED = ConfigProperty
            .ofBoolean("config-delete-orphans")
            .defaultValue(true);

        ConfigProperty<Boolean> IS_DELETE_ORPHANS_ENABLED = ConfigProperty
            .ofBoolean("delete-orphans")
            .defaultValue(false);
    }

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private AivenKafkaTopicCollector collector;

    private AivenApiClientConfig apiClientConfig;

    private List<Pattern> topicDeleteExcludePatterns;

    /**
     * Creates a new {@link AivenKafkaTopicController} instance.
     * CLI requires any empty constructor.
     */
    public AivenKafkaTopicController() {
        super();
    }

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryController} instance.
     *
     * @param apiClientConfig the schema registry client configuration.
     */
    public AivenKafkaTopicController(@NotNull AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        AivenExtensionProvider provider = context.provider();

        init(provider.apiClientConfig());
        this.topicDeleteExcludePatterns = provider.topicDeleteExcludePatterns();
    }

    private void init(@NotNull AivenApiClientConfig apiClientConfig) {
        if (this.initialized.compareAndSet(false, true)) {
            this.apiClientConfig = apiClientConfig;
            this.collector = new AivenKafkaTopicCollector(apiClientConfig);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull final ReconciliationContext context) {

        try (AivenApiClient api = AivenApiClientFactory.create(apiClientConfig)) {
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                new KafkaTopicChangeHandler.Create(api),
                new KafkaTopicChangeHandler.Update(api),
                new KafkaTopicChangeHandler.Delete(api),
                new KafkaTopicChangeHandler.None()
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
            KafkaConfigsConfig.DEFAULT_CONFIGS.key(), true,
            KafkaConfigsConfig.DYNAMIC_BROKER_CONFIGS.key(), true,
            KafkaConfigsConfig.STATIC_BROKER_CONFIGS.key(), true
        ));

        // Get the list of remote resources that are candidates for this reconciliation
        collector.init(extensionContext().contextForExtension(AivenKafkaTopicCollector.class));

        List<V1KafkaTopic> actualKafkaTopics = collector.listAll(configuration, Selectors.NO_SELECTOR)
            .stream()
            .filter(context.selector()::apply)
            .toList();

        TopicChangeComputer changeComputer = new TopicChangeComputer(
            topicDeleteExcludePatterns,
            Config.IS_DELETE_ORPHANS_ENABLED.get(context.configuration()),
            Config.IS_CONFIG_DELETE_ORPHANS_ENABLED.get(context.configuration())
        );

        return changeComputer.computeChanges(actualKafkaTopics, expectedKafkaTopics)
            .stream()
            .map(change -> change.withApiVersion(KAFKA_AIVEN_V1BETA2))
            .toList();
    }
}
