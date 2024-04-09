/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
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
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.Modes;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectCollector extends ContextualExtension implements Collector<V1SchemaRegistrySubject> {

    private SchemaRegistryClientConfig config;

    private boolean prettyPrintSchema = true;

    private boolean defaultToGlobalCompatibilityLevel = true;

    private boolean defaultToGlobalMode = true;

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

        AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config));
        try {
            CompletableFuture<List<V1SchemaRegistrySubject>> result = api
                    .listSubjects()
                    .thenComposeAsync(subjects -> AsyncUtils.waitForAll(getAllSchemaRegistrySubjectsAsync(subjects, api)));
            Optional<Throwable> exception = AsyncUtils.getException(result);
            if (exception.isPresent()) {
                throw new JikkouRuntimeException(
                        "Failed to list all schema registry subject versions",
                        exception.get()
                );
            }
            List<V1SchemaRegistrySubject> resources = result.join()
                    .stream()
                    .filter(selector::apply)
                    .collect(Collectors.toList());
            return new V1SchemaRegistrySubjectList(resources);
        } finally {
            api.close();
        }
    }

    public SchemaRegistrySubjectCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    public SchemaRegistrySubjectCollector defaultToGlobalCompatibilityLevel(final boolean defaultToGlobalCompatibilityLevel) {
        this.defaultToGlobalCompatibilityLevel = defaultToGlobalCompatibilityLevel;
        return this;
    }

    public SchemaRegistrySubjectCollector defaultToGlobalMode(final boolean defaultToGlobalMode) {
        this.defaultToGlobalMode = defaultToGlobalMode;
        return this;
    }

    @NotNull
    private List<CompletableFuture<V1SchemaRegistrySubject>> getAllSchemaRegistrySubjectsAsync(final List<String> subjects,
                                                                                               final AsyncSchemaRegistryApi api) {
        return subjects.stream()
                .map(subject -> getSchemaRegistrySubjectAsync(
                        api, subject, defaultToGlobalCompatibilityLevel, defaultToGlobalMode, schemaRegistrySubjectFactory))
                .toList();
    }

    @NotNull
    private static CompletableFuture<V1SchemaRegistrySubject> getSchemaRegistrySubjectAsync(@NotNull AsyncSchemaRegistryApi api,
                                                                                            @NotNull String subject,
                                                                                            boolean defaultToGlobalCompatibilityLevel,
                                                                                            boolean defaultToGlobalMode,
                                                                                            @NotNull V1SchemaRegistrySubjectFactory factory) {

        CompletableFuture<SubjectSchemaVersion> schemaSubjectFuture = api.getLatestSubjectSchema(subject);
        CompletableFuture<CompatibilityLevels> compatibilityLevelsFuture = getSubjectCompatibilityLevel(api, subject, defaultToGlobalCompatibilityLevel)
                .exceptionally(SchemaRegistrySubjectCollector::handleNotFound);
        CompletableFuture<Modes> modesFuture = getSubjectMode(api, subject, defaultToGlobalMode)
                .exceptionally(SchemaRegistrySubjectCollector::handleNotFound);

        return CompletableFuture.allOf(schemaSubjectFuture, compatibilityLevelsFuture, modesFuture)
                .thenApplyAsync(it -> {
                    SubjectSchemaVersion subjectSchema = schemaSubjectFuture.join();
                    CompatibilityLevels compatibilityLevels = compatibilityLevelsFuture.join();
                    Modes modes = modesFuture.join();

                    return factory.createSchemaRegistrySubject(subjectSchema, compatibilityLevels, modes);
                });
    }

    private static CompletableFuture<CompatibilityLevels> getSubjectCompatibilityLevel(@NotNull AsyncSchemaRegistryApi api,
                                                                                       @NotNull String subject,
                                                                                       boolean defaultToGlobalCompatibilityLevel) {
        return api.getSubjectCompatibilityLevel(subject, defaultToGlobalCompatibilityLevel)
                .thenApply(compatibilityObject -> CompatibilityLevels.valueOf(compatibilityObject.compatibilityLevel()));
    }

    private static CompletableFuture<Modes> getSubjectMode(@NotNull AsyncSchemaRegistryApi api,
                                                           @NotNull String subject,
                                                           boolean defaultToGlobalMode) {
        return api.getSubjectMode(subject, defaultToGlobalMode)
                .thenApply(modeObject -> Modes.valueOf(modeObject.mode()));
    }

    private static <T> T handleNotFound(Throwable t) {
        if (t.getCause() != null) t = t.getCause();

        if (t instanceof RestClientException exception) {
            if (exception.response()
                    .map(Response::getStatus)
                    .filter(status -> status.equals(404))
                    .isPresent()) {
                return null;
            }
        }
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new JikkouRuntimeException(t);
    }
}
