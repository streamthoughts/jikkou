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
package io.streamthoughts.jikkou.extension.aiven.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.resource.ResourceCollector;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.core.selectors.ResourceSelector;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaRegistryAclResponse;
import io.streamthoughts.jikkou.extension.aiven.converter.V1SchemaRegistryAclEntryListConverter;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntryList;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1SchemaRegistryAclEntry.class)
@AcceptsResource(type = V1SchemaRegistryAclEntryList.class, converter = V1SchemaRegistryAclEntryListConverter.class)
public final class AivenSchemaRegistryAclEntryCollector implements ResourceCollector<V1SchemaRegistryAclEntry> {

    private AivenApiClientConfig config;

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     */
    public AivenSchemaRegistryAclEntryCollector() {}

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     *
     * @param config the configuration.
     */
    public AivenSchemaRegistryAclEntryCollector(AivenApiClientConfig config) {
        configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new AivenApiClientConfig(config));
    }

    private void configure(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<V1SchemaRegistryAclEntry> listAll(@NotNull Configuration configuration,
                                              @NotNull List<ResourceSelector> selectors) {
        AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            ListSchemaRegistryAclResponse response = api.listSchemaRegistryAclEntries();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                        String.format("failed to list kafka acl entries. %s (%s)",
                                response.message(),
                                response.errors()
                        )
                );
            }

            return SchemaRegistryAclEntryAdapter.map(response.acl())
                    .stream()
                    .filter(new AggregateSelector(selectors)::apply)
                    .collect(Collectors.toList());

        } catch (RestClientException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponseEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponseEntity();
            }
            throw new JikkouRuntimeException(String.format(
                    "failed to list schema registry acl entries. %s:%n%s",
                    e.getLocalizedMessage(),
                    response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }
}
