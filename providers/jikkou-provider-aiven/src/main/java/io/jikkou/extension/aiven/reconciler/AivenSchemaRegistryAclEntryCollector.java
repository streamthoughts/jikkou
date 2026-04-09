/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.io.Jackson;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.extension.aiven.AivenExtensionProvider;
import io.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.jikkou.extension.aiven.api.AivenApiClient;
import io.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.jikkou.extension.aiven.api.AivenApiClientException;
import io.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.jikkou.extension.aiven.api.data.ListSchemaRegistryAclResponse;
import io.jikkou.extension.aiven.collections.V1SchemaRegistryAclEntryList;
import io.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Title("Collect Aiven Schema Registry ACLs")
@Description("Collects Schema Registry ACL entry resources from an Aiven service.")
@SupportedResource(type = V1SchemaRegistryAclEntry.class)
public final class AivenSchemaRegistryAclEntryCollector implements Collector<V1SchemaRegistryAclEntry> {

    private AivenApiClientConfig apiClientConfig;

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     */
    public AivenSchemaRegistryAclEntryCollector() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     *
     * @param apiClientConfig the configuration.
     */
    public AivenSchemaRegistryAclEntryCollector(AivenApiClientConfig apiClientConfig) {
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
    public ResourceList<V1SchemaRegistryAclEntry> listAll(@NotNull Configuration configuration,
                                                          @NotNull Selector selector) {
        AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
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

            List<V1SchemaRegistryAclEntry> items = SchemaRegistryAclEntryAdapter.map(response.acl())
                    .stream()
                    .filter(selector::apply)
                    .collect(Collectors.toList());
            return new V1SchemaRegistryAclEntryList.Builder().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
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
