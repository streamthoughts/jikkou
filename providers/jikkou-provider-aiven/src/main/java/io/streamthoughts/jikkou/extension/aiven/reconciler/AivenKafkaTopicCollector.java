/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import static io.streamthoughts.jikkou.kafka.reconciler.KafkaConfigsConfig.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.generics.GenericResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaTopicAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicConfigInfo;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicListResponse;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.reconciler.KafkaConfigsConfig;
import jakarta.ws.rs.WebApplicationException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

@SupportedResource(kind = "KafkaTopic", apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA2)
public class AivenKafkaTopicCollector extends ContextualExtension implements Collector<V1KafkaTopic> {

    private AivenApiClientConfig apiClientConfig;

    /**
     * Creates a new {@link AivenKafkaTopicCollector} instance.
     */
    public AivenKafkaTopicCollector() {}

    /**
     * Creates a new {@link AivenKafkaTopicCollector} instance.
     *
     * @param apiClientConfig the configuration.
     */
    public AivenKafkaTopicCollector(final AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig apiClientConfig) throws ConfigException {
        this.apiClientConfig = apiClientConfig;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1KafkaTopic> listAll(@NotNull Configuration configuration,
                                              @NotNull Selector selector) {
        final AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            KafkaTopicListResponse response = api.listKafkaTopics();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                    String.format("Failed to list kafka topics. %s (%s)",
                        response.message(),
                        response.errors()
                    )
                );
            }
            List<V1KafkaTopic> items = response.topics()
                .stream()
                .map(topicInfo -> api.getKafkaTopicInfo(topicInfo.topicName()))
                .map(KafkaTopicInfoResponse::topic)
                .filter(Objects::nonNull)
                .map(topicInfo -> KafkaTopicAdapter.map(topicInfo, getConfigPredicate(configuration)))
                .filter(selector::apply)
                .toList();
            
            return new GenericResourceList.Builder<V1KafkaTopic>().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
            }
            throw new AivenApiClientException(String.format(
                "Failed to list kafka topics. %s:%n%s",
                e.getLocalizedMessage(),
                response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            DEFAULT_CONFIGS,
            DYNAMIC_BROKER_CONFIGS,
            STATIC_BROKER_CONFIGS
        );
    }

    private Predicate<KafkaTopicConfigInfo> getConfigPredicate(final Configuration configuration) {
        Set<KafkaTopicConfigInfo.Source> sources = new HashSet<>();
        sources.add(KafkaTopicConfigInfo.Source.TOPIC_CONFIG);
        if (KafkaConfigsConfig.DEFAULT_CONFIGS.get(configuration)) {
            sources.add(KafkaTopicConfigInfo.Source.DEFAULT_CONFIG);
        }

        if (KafkaConfigsConfig.DYNAMIC_BROKER_CONFIGS.get(configuration)) {
            sources.add(KafkaTopicConfigInfo.Source.DYNAMIC_BROKER_CONFIG);
            sources.add(KafkaTopicConfigInfo.Source.DYNAMIC_DEFAULT_BROKER_CONFIG);
        }

        if (KafkaConfigsConfig.STATIC_BROKER_CONFIGS.get(configuration)) {
            sources.add(KafkaTopicConfigInfo.Source.STATIC_BROKER_CONFIG);
        }
        return kafkaTopicConfigInfo -> sources.contains(kafkaTopicConfigInfo.source());
    }
}
