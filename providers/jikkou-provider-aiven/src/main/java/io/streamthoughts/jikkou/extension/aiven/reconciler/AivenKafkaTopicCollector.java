/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaTopicAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoResponse;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicListResponse;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@SupportedResource(kind = "KafkaTopic", apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1)
public class AivenKafkaTopicCollector implements Collector<V1KafkaTopic> {

    private AivenApiClientConfig config;

    /**
     * Creates a new {@link AivenKafkaTopicCollector} instance.
     */
    public AivenKafkaTopicCollector() {}

    /**
     * Creates a new {@link AivenKafkaTopicCollector} instance.
     *
     * @param config the configuration.
     */
    public AivenKafkaTopicCollector(final AivenApiClientConfig config) {
        init(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(new AivenApiClientConfig(context.appConfiguration()));
    }

    private void init(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<V1KafkaTopic> listAll(@NotNull Configuration configuration,
                                                    @NotNull Selector selector) {
        final AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            KafkaTopicListResponse response = api.listKafkaTopics();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                    String.format("failed to list kafka topics. %s (%s)",
                        response.message(),
                        response.errors()
                    )
                );
            }
            List<V1KafkaTopic> items = response.topics()
                .stream()
                .map(topicInfo -> api.getKafkaTopicInfo(topicInfo.topicName()))
                .map(KafkaTopicInfoResponse::topic)
                .map(KafkaTopicAdapter::map)
                .filter(selector::apply)
                .toList();
            
            return DefaultResourceListObject
                .<V1KafkaTopic>builder()
                .withItems(items)
                .build();

        } catch (RestClientException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(e.getResponseEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponseEntity();
            }
            throw new AivenApiClientException(String.format(
                "failed to list kafka topics. %s:%n%s",
                e.getLocalizedMessage(),
                response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }
}
