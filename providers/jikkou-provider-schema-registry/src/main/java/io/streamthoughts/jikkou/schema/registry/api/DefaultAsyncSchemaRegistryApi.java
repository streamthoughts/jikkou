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
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around the REST API {@link SchemaRegistryApi} to provide asynchronous methods.
 */
public final class DefaultAsyncSchemaRegistryApi implements AutoCloseable, AsyncSchemaRegistryApi {

    private final SchemaRegistryApi api;

    /**
     * Creates a new {@link DefaultAsyncSchemaRegistryApi} instance.
     *
     * @param api the {@link SchemaRegistryApi} to delegate methods called.
     */
    public DefaultAsyncSchemaRegistryApi(final @NotNull SchemaRegistryApi api) {
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    /**
     * @see SchemaRegistryApi#listSubjects()
     */
    @Override
    public CompletableFuture<List<String>> listSubjects() {
        return CompletableFuture.supplyAsync(api::listSubjects);
    }

    /**
     * @see SchemaRegistryApi#deleteSubjectVersions(String, boolean)
     */
    @Override
    public CompletableFuture<List<Integer>> deleteSubjectVersions(@NotNull final String subject,
                                                                  boolean permanent) {
        return CompletableFuture.supplyAsync(() -> api.deleteSubjectVersions(subject, permanent));
    }

    /**
     * @see SchemaRegistryApi#registerSchema(String, SubjectSchemaRegistration, boolean)
     */
    @Override
    public CompletableFuture<SubjectSchemaId> registerSubjectVersion(@NotNull final String subject,
                                                                     @NotNull final SubjectSchemaRegistration schema,
                                                                     boolean normalize) {
        return CompletableFuture.supplyAsync(() -> api.registerSchema(subject, schema, normalize));
    }


    /**
     * @see SchemaRegistryApi#getLatestSubjectSchema(String)
     */
    @Override
    public CompletableFuture<SubjectSchemaVersion> getLatestSubjectSchema(@NotNull final String subject) {
        return CompletableFuture.supplyAsync(() -> api.getLatestSubjectSchema(subject));
    }

    /**
     * @see SchemaRegistryApi#getGlobalCompatibility()
     */
    @Override
    public CompletableFuture<CompatibilityLevelObject> getGlobalCompatibility() {
        return CompletableFuture.supplyAsync(api::getGlobalCompatibility);
    }

    /**
     * @see SchemaRegistryApi#getConfigCompatibility(String, boolean)
     */
    @Override
    public CompletableFuture<CompatibilityLevelObject> getSubjectCompatibilityLevel(@NotNull final String subject,
                                                                                    boolean defaultToGlobal) {
        return CompletableFuture.supplyAsync(() -> api.getConfigCompatibility(subject, defaultToGlobal));
    }

    /**
     * @see SchemaRegistryApi#updateConfigCompatibility(String, CompatibilityObject)
     */
    @Override
    public CompletableFuture<CompatibilityObject> updateSubjectCompatibilityLevel(@NotNull final String subject,
                                                                                  @NotNull final CompatibilityObject compatibility) {
        return CompletableFuture.supplyAsync(() -> api.updateConfigCompatibility(subject, compatibility));
    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    @Override
    public CompletableFuture<CompatibilityObject> deleteSubjectCompatibilityLevel(@NotNull final String subject) {
        return CompletableFuture.supplyAsync(() -> api.deleteConfigCompatibility(subject));

    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    @Override
    public CompletableFuture<CompatibilityCheck> testCompatibility(@NotNull final String subject,
                                                                   String version,
                                                                   boolean verbose,
                                                                   @NotNull final SubjectSchemaRegistration schema) {
        return CompletableFuture.supplyAsync(
                () -> api.testCompatibility(subject, Integer.parseInt(version), verbose, schema)
        );
    }

    /**
     * @see SchemaRegistryApi#testCompatibilityLatest(String, boolean, SubjectSchemaRegistration)
     */
    @Override
    public CompletableFuture<CompatibilityCheck> testCompatibilityLatest(@NotNull String subject,
                                                                         boolean verbose,
                                                                         @NotNull SubjectSchemaRegistration schema) {
        return CompletableFuture.supplyAsync(
                () -> api.testCompatibilityLatest(subject, verbose, schema)
        );
    }

    @Override
    public void close() {
        this.api.close();
    }
}
