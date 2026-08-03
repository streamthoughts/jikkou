/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import static io.jikkou.kafka.reconciler.KafkaConfigsConfig.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.io.Jackson;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.generics.GenericResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.core.selector.Selectors;
import io.jikkou.extension.aiven.AivenExtensionProvider;
import io.jikkou.extension.aiven.ApiVersions;
import io.jikkou.extension.aiven.adapter.KafkaTopicAdapter;
import io.jikkou.extension.aiven.api.AivenApiClient;
import io.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.jikkou.extension.aiven.api.AivenApiClientException;
import io.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.jikkou.extension.aiven.api.data.KafkaTopicConfigInfo;
import io.jikkou.extension.aiven.api.data.KafkaTopicInfo;
import io.jikkou.extension.aiven.api.data.KafkaTopicListResponse;
import io.jikkou.kafka.models.V1KafkaTopic;
import io.jikkou.kafka.reconciler.KafkaConfigsConfig;
import jakarta.ws.rs.WebApplicationException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

@Title("Collect Aiven Kafka topics")
@Description("Collects Kafka topic resources from an Aiven service.")
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
            List<String> topics = response.topics()
                .stream()
                .map(KafkaTopicListResponse.KafkaTopicInfoGet::topicName)
                .toList();

            return listAll(configuration, topics, selector, api);

        } catch (WebApplicationException e) {
            throw newListException(e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }

    /**
     * Lists only the given Kafka topics, by name.
     *
     * <p>In contrast to {@link #listAll(Configuration, Selector)}, this does not list every topic
     * of the service first: it describes the given topics directly, so the number of API calls
     * scales with the number of topics asked for and not with the size of the service. Topics that
     * do not exist are skipped.
     *
     * @param configuration the configuration.
     * @param topics        the names of the topics to describe.
     * @return the list of described topics that exist.
     */
    public ResourceList<V1KafkaTopic> listAll(@NotNull Configuration configuration,
                                              @NotNull List<String> topics) {
        final AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            return listAll(configuration, topics, Selectors.NO_SELECTOR, api);
        } catch (WebApplicationException e) {
            throw newListException(e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }

    private ResourceList<V1KafkaTopic> listAll(@NotNull Configuration configuration,
                                               @NotNull List<String> topics,
                                               @NotNull Selector selector,
                                               @NotNull AivenApiClient api) {
        List<V1KafkaTopic> items = topics
            .stream()
            .map(topic -> describeTopicOrEmptyOn404(api, topic))
            .filter(Objects::nonNull)
            .map(topicInfo -> KafkaTopicAdapter.map(topicInfo, getConfigPredicate(configuration)))
            .filter(selector::apply)
            .toList();

        return new GenericResourceList.Builder<V1KafkaTopic>().withItems(items).build();
    }

    /**
     * Describes a single topic, returning {@code null} if it does not exist. A requested topic may
     * legitimately be missing (e.g. it is about to be created), which is not an error.
     */
    private KafkaTopicInfo describeTopicOrEmptyOn404(@NotNull AivenApiClient api,
                                                     @NotNull String topic) {
        try {
            return api.getKafkaTopicInfo(topic).topic();
        } catch (WebApplicationException e) {
            if (isNotFound(e)) {
                return null;
            }
            throw e;
        }
    }

    private static boolean isNotFound(final WebApplicationException exception) {
        return exception.getResponse().getStatus() == 404;
    }

    private AivenApiClientException newListException(@NotNull WebApplicationException e) {
        String response;
        try {
            response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
        } catch (JsonProcessingException ex) {
            response = e.getResponse().readEntity(String.class);
        }
        return new AivenApiClientException(String.format(
            "Failed to list kafka topics. %s:%n%s",
            e.getLocalizedMessage(),
            response
        ), e);
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
