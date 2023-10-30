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
package io.streamthoughts.jikkou.schema.registry.control;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selectors.ResourceSelector;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.V1SchemaRegistrySubjectFactory;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectCollector implements Collector<V1SchemaRegistrySubject> {

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
        configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new SchemaRegistryClientConfig(config));
    }

    private void configure(@NotNull SchemaRegistryClientConfig config) throws ConfigException {
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
    public List<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                 @NotNull List<ResourceSelector> selectors) {

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
            return result.join();
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

    @NotNull
    private List<CompletableFuture<V1SchemaRegistrySubject>> getAllSchemaRegistrySubjectsAsync(final List<String> subjects,
                                                                                               final AsyncSchemaRegistryApi api) {
        return subjects.stream()
                .map(subject -> getSchemaRegistrySubjectAsync(
                        api, subject, defaultToGlobalCompatibilityLevel, schemaRegistrySubjectFactory))
                .toList();
    }

    @NotNull
    private static CompletableFuture<V1SchemaRegistrySubject> getSchemaRegistrySubjectAsync(@NotNull AsyncSchemaRegistryApi api,
                                                                                            @NotNull String subject,
                                                                                            boolean defaultToGlobalCompatibilityLevel,
                                                                                            @NotNull V1SchemaRegistrySubjectFactory factory) {

        return api
                // Get Schema Registry Latest Subject Version
                .getLatestSubjectSchema(subject)
                // Get Schema Registry Subject Compatibility
                .thenCompose(subjectSchemaVersion -> api
                        .getSubjectCompatibilityLevel(subject, defaultToGlobalCompatibilityLevel)
                        .thenApply(compatibilityObject -> CompatibilityLevels.valueOf(compatibilityObject.compatibilityLevel()))
                        .exceptionally(t -> {
                                    if (t.getCause() != null) t = t.getCause();

                                    if (t instanceof RestClientException exception) {
                                        if (exception.response().getStatus() == 404)
                                            return null;
                                    }
                                    if (t instanceof RuntimeException re) {
                                        throw re;
                                    }
                                    throw new JikkouRuntimeException(t);
                                }
                        )
                        .thenApply(compatibilityLevelObject ->
                                Pair.of(subjectSchemaVersion, compatibilityLevelObject)
                        )
                )
                // Create SchemaRegistrySubject object
                .thenApply(tuple ->
                        factory.createSchemaRegistrySubject(
                                tuple._1(),
                                tuple._2()
                        )
                );
    }
}
