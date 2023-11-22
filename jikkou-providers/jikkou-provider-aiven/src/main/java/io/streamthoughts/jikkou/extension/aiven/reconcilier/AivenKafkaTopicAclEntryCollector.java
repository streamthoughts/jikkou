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
package io.streamthoughts.jikkou.extension.aiven.reconcilier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaAclResponse;
import io.streamthoughts.jikkou.extension.aiven.collections.V1KafkaTopicAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.http.client.RestClientException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaTopicAclEntry.class)
public class AivenKafkaTopicAclEntryCollector implements Collector<V1KafkaTopicAclEntry> {

    private AivenApiClientConfig config;

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryCollector} instance.
     */
    public AivenKafkaTopicAclEntryCollector() {
    }

    /**
     * Creates a new {@link AivenKafkaTopicAclEntryCollector} instance.
     *
     * @param config the configuration.
     */
    public AivenKafkaTopicAclEntryCollector(AivenApiClientConfig config) {
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
    public ResourceListObject<V1KafkaTopicAclEntry> listAll(@NotNull Configuration configuration,
                                                            @NotNull Selector selector) {
        AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            ListKafkaAclResponse response = api.listKafkaAclEntries();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                        String.format("failed to list kafka acl entries. %s (%s)",
                                response.message(),
                                response.errors()
                        )
                );
            }

            List<V1KafkaTopicAclEntry> items = KafkaAclEntryAdapter.map(response.acl())
                    .stream()
                    .filter(selector::apply)
                    .collect(Collectors.toList());

            return new V1KafkaTopicAclEntryList(items);

        } catch (RestClientException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponseEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponseEntity();
            }
            throw new AivenApiClientException(String.format(
                    "failed to list kafka acl entries. %s:%n%s",
                    e.getLocalizedMessage(),
                    response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }
}
