/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
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
@SupportedResource(
        apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
        kind = ApiVersions.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaRegistrySubjectCollector implements Collector<V1SchemaRegistrySubject> {

    private static final String SCHEMA_REGISTRY_VENDOR = "Karapace";

    private AivenApiClientConfig apiClientConfig;
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
        init(configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.apiClientConfig = config;
        this.schemaRegistrySubjectFactory = new V1SchemaRegistrySubjectFactory(
                SCHEMA_REGISTRY_VENDOR,
                config.apiUrl(),
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
    public ResourceList<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                         @NotNull Selector selector) {
        AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
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
                    .map(subject -> subject.withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1))
                    .toList();
            return new V1SchemaRegistrySubjectList.Builder().withItems(items).build();

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
                .thenApply(pair ->
                        // Create SchemaRegistrySubject object
                        factory.createSchemaRegistrySubject(pair._1().version(), pair._2().compatibilityLevel(), null)
                );
    }
}
