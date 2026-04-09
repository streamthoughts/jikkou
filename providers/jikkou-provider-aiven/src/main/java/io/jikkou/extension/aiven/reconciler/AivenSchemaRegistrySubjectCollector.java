/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.common.utils.AsyncUtils;
import io.jikkou.common.utils.Pair;
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
import io.jikkou.extension.aiven.ApiVersions;
import io.jikkou.extension.aiven.api.AivenApiClient;
import io.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.jikkou.extension.aiven.api.AivenApiClientException;
import io.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.jikkou.extension.aiven.api.data.ListSchemaSubjectsResponse;
import io.jikkou.extension.aiven.collections.V1SchemaRegistrySubjectList;
import io.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Aiven - Schema Registry Subjects Collector.
 */
@Title("Collect Aiven Schema Registry subjects")
@Description("Collects Schema Registry subject resources from an Aiven service.")
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
                if (error instanceof WebApplicationException wae)
                    throw wae;

                throw new AivenApiClientException("Failed to list schema registry subject versions", error);
            }

            List<V1SchemaRegistrySubject> items = result.join().stream()
                    .map(subject -> subject.withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1))
                    .toList();
            return new V1SchemaRegistrySubjectList.Builder().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
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
