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
import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaSubjectsResponse;
import io.streamthoughts.jikkou.extension.aiven.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Aiven - Schema Registry Subjects Collector.
 */
@HandledResource(
        apiVersion = AivenExtensionProvider.SCHEMA_REGISTRY_API_VERSION,
        kind = AivenExtensionProvider.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaRegistrySubjectCollector implements Collector<V1SchemaRegistrySubject> {

    private static final String SCHEMA_REGISTRY_VENDOR = "Karapace";

    private AivenApiClientConfig configuration;
    private boolean prettyPrintSchema = true;
    private V1SchemaRegistrySubjectFactory schemaRegistrySubjectFactory;

    /**
     * Creates a new {@link AivenSchemaRegistrySubjectCollector} instance.
     */
    public AivenSchemaRegistrySubjectCollector() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistrySubjectCollector} instance.
     *
     * @param configuration the configuration.
     */
    public AivenSchemaRegistrySubjectCollector(AivenApiClientConfig configuration) {
        configure(configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new AivenApiClientConfig(config));
    }

    private void configure(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.configuration = config;
        this.schemaRegistrySubjectFactory = new V1SchemaRegistrySubjectFactory(
                SCHEMA_REGISTRY_VENDOR,
                config.getApiUrl(),
                prettyPrintSchema
        );
    }

    public AivenSchemaRegistrySubjectCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                               @NotNull List<Selector> selectors) {
        AivenApiClient api = AivenApiClientFactory.create(this.configuration);
        try {
            ListSchemaSubjectsResponse response = api.listSchemaRegistrySubjects();

            if (!response.errors().isEmpty()) {
                throw new JikkouRuntimeException(
                        String.format("failed to list kafka schema registry subjects. %s (%s)",
                                response.message(),
                                response.errors()
                        )
                );
            }

            CompletableFuture<List<V1SchemaRegistrySubject>> result = AsyncUtils
                    .waitForAll(getAllSchemaRegistrySubjectsAsync(response.subjects(), api));

            Optional<Throwable> exception = AsyncUtils.getException(result);
            if (exception.isPresent()) {
                Throwable error = exception.get();
                if (error instanceof RestClientException rce)
                    throw rce;

                throw new AivenApiClientException("Failed to list schema registry subject versions", error);
            }

            List<V1SchemaRegistrySubject> items = result.join().stream()
                    .map(subject -> subject.withApiVersion(AivenExtensionProvider.SCHEMA_REGISTRY_API_VERSION))
                    .toList();
            return new V1SchemaRegistrySubjectList(items);

        } catch (RestClientException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponseEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponseEntity();
            }
            throw new AivenApiClientException(String.format(
                    "failed to list schema registry subject versions. %s:%n%s",
                    e.getLocalizedMessage(),
                    response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }

    @NotNull
    private List<CompletableFuture<V1SchemaRegistrySubject>> getAllSchemaRegistrySubjectsAsync(@NotNull List<String> subjects,
                                                                                               @NotNull AivenApiClient api) {
        return subjects.stream()
                .map(subject -> getSchemaRegistrySubjectAsync(api, subject, schemaRegistrySubjectFactory))
                .toList();
    }

    @NotNull
    private static CompletableFuture<V1SchemaRegistrySubject> getSchemaRegistrySubjectAsync(@NotNull AivenApiClient api,
                                                                                            @NotNull String subject,
                                                                                            @NotNull V1SchemaRegistrySubjectFactory factory) {
        return CompletableFuture
                .supplyAsync(
                        // Get Schema Registry Latest Subject Version
                        () -> api.getSchemaRegistryLatestSubjectVersion(subject))
                .thenCompose(subjectSchemaVersion -> CompletableFuture.supplyAsync(
                        // Get Subject Compatibility
                        () -> Pair.of(subjectSchemaVersion, api.getSchemaRegistrySubjectCompatibility(subject))))
                .thenApply(tuple ->
                        // Create SchemaRegistrySubject object
                        factory.createSchemaRegistrySubject(tuple._1().version(), tuple._2().compatibilityLevel())
                );
    }
}
