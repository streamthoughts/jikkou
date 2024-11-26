/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.http.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectCollector extends ContextualExtension implements Collector<V1SchemaRegistrySubject> {

    private SchemaRegistryClientConfig config;

    private boolean prettyPrintSchema = true;

    private boolean defaultToGlobalCompatibilityLevel = true;

    private V1SchemaRegistrySubjectFactory schemaRegistrySubjectFactory;

    /**
     * Creates a new {@link SchemaRegistrySubjectCollector} instance.
     */
    public SchemaRegistrySubjectCollector() {
    }

    /**
     * Creates a new {@link SchemaRegistrySubjectCollector} instance.
     *
     * @param config the configuration.
     */
    public SchemaRegistrySubjectCollector(SchemaRegistryClientConfig config) {
        init(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        init(new SchemaRegistryClientConfig(context.appConfiguration()));
    }

    private void init(@NotNull SchemaRegistryClientConfig config) {
        this.config = config;
        this.schemaRegistrySubjectFactory = new V1SchemaRegistrySubjectFactory(
                config.getSchemaRegistryVendor(),
                config.getSchemaRegistryUrl(),
                prettyPrintSchema
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                               @NotNull Selector selector) {

        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config))) {
            return listAll(api.listSubjects().flatMapMany(Flux::fromIterable), api);
        }
    }

    public ResourceListObject<V1SchemaRegistrySubject> listAll(@NotNull List<String> subjects) {
        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config))) {
            return listAll(Flux.fromIterable(subjects), api);
        }
    }

    private ResourceListObject<V1SchemaRegistrySubject> listAll(@NotNull Flux<String> subjects,
                                                                @NotNull AsyncSchemaRegistryApi api) {
        Flux<V1SchemaRegistrySubject> flux = subjects
            // Get Schema Registry Latest Subject Version
            .flatMap(api::getLatestSubjectSchema)
            .onErrorResume(t -> t instanceof RestClientException rce && isNotFound(rce) ? Mono.empty() : Mono.error(t))
            // Get Schema Registry Subject Compatibility
            .flatMap(subjectSchemaVersion -> api
                    .getSubjectCompatibilityLevel(subjectSchemaVersion.subject(), defaultToGlobalCompatibilityLevel)
                    .map(compatibilityObject ->
                            CompatibilityLevels.valueOf(compatibilityObject.compatibilityLevel()))
                    .map(compatibilityLevels ->
                            schemaRegistrySubjectFactory.createSchemaRegistrySubject(subjectSchemaVersion, compatibilityLevels))
                    .onErrorResume(t -> t instanceof RestClientException rce && isNotFound(rce) ?
                            Mono.just(schemaRegistrySubjectFactory.createSchemaRegistrySubject(subjectSchemaVersion, null)) :
                            Mono.error(t))
            );
        try {
            return new V1SchemaRegistrySubjectList(flux.collectList().block());
        } catch (Exception e) {
            throw new JikkouRuntimeException("Failed to list all schema registry subject versions", e);
        }
    }

    private static boolean isNotFound(final RestClientException exception) {
        return exception.response()
                .map(Response::getStatus)
                .filter(status -> status.equals(404))
                .isPresent();
    }

    public SchemaRegistrySubjectCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    public SchemaRegistrySubjectCollector defaultToGlobalCompatibilityLevel(final boolean defaultToGlobalCompatibilityLevel) {
        this.defaultToGlobalCompatibilityLevel = defaultToGlobalCompatibilityLevel;
        return this;
    }
}
