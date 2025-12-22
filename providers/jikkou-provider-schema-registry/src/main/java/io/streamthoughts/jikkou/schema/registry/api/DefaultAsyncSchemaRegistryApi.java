/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ModeObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

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
    public Mono<List<String>> listSubjects() {
        return Mono.fromCallable(api::listSubjects);
    }

    /**
     * @see SchemaRegistryApi#deleteSubjectVersions(String, boolean)
     */
    @Override
    public Mono<List<Integer>> deleteSubjectVersions(@NotNull final String subject,
                                                     boolean permanent) {
        return Mono.fromCallable(() -> api.deleteSubjectVersions(subject, permanent));
    }

    /**
     * @see SchemaRegistryApi#registerSchema(String, SubjectSchemaRegistration, boolean)
     */
    @Override
    public Mono<SubjectSchemaId> registerSubjectVersion(@NotNull final String subject,
                                                        @NotNull final SubjectSchemaRegistration schema,
                                                        boolean normalize) {
        return Mono.fromCallable(() -> api.registerSchema(subject, schema, normalize));
    }


    /**
     * @see SchemaRegistryApi#getLatestSubjectSchema(String)
     */
    @Override
    public Mono<SubjectSchemaVersion> getLatestSubjectSchema(@NotNull final String subject) {
        return Mono.fromCallable(() -> api.getLatestSubjectSchema(subject));
    }

    /**
     * @see SchemaRegistryApi#getGlobalCompatibility()
     */
    @Override
    public Mono<CompatibilityLevelObject> getGlobalCompatibility() {
        return Mono.fromCallable(api::getGlobalCompatibility);
    }

    /**
     * @see SchemaRegistryApi#getConfigCompatibility(String, boolean)
     */
    @Override
    public Mono<CompatibilityLevelObject> getSubjectCompatibilityLevel(@NotNull final String subject,
                                                                       boolean defaultToGlobal) {
        return Mono.fromCallable(() -> api.getConfigCompatibility(subject, defaultToGlobal));
    }

    /**
     * @see SchemaRegistryApi#updateConfigCompatibility(String, CompatibilityObject)
     */
    @Override
    public Mono<CompatibilityObject> updateSubjectCompatibilityLevel(@NotNull final String subject,
                                                                     @NotNull final CompatibilityObject compatibility) {
        return Mono.fromCallable(() -> api.updateConfigCompatibility(subject, compatibility));
    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    @Override
    public Mono<CompatibilityObject> deleteSubjectCompatibilityLevel(@NotNull final String subject) {
        return Mono.fromCallable(() -> api.deleteConfigCompatibility(subject));

    }

    @Override
    public Mono<ModeObject> getSubjectMode(@NotNull String subject) {
        return Mono.fromCallable(() -> api.getMode(subject));
    }

    @Override
    public Mono<ModeObject> updateSubjectMode(@NotNull String subject, @NotNull ModeObject mode) {
        return Mono.fromCallable(() -> api.updateMode(subject, mode));
    }

    @Override
    public Mono<ModeObject> deleteSubjectMode(@NotNull final String subject) {
        return Mono.fromCallable(() -> api.deleteMode(subject));
    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    @Override
    public Mono<CompatibilityCheck> testCompatibility(@NotNull final String subject,
                                                      String version,
                                                      boolean verbose,
                                                      @NotNull final SubjectSchemaRegistration schema) {
        return Mono.fromCallable(
            () -> api.testCompatibility(subject, Integer.parseInt(version), verbose, schema)
        );
    }

    /**
     * @see SchemaRegistryApi#getMode()
     */
    @Override
    public Mono<ModeObject> getGlobalMode() {
        return Mono.fromCallable(api::getMode);
    }

    /**
     * @see SchemaRegistryApi#testCompatibilityLatest(String, boolean, SubjectSchemaRegistration)
     */
    @Override
    public Mono<CompatibilityCheck> testCompatibilityLatest(@NotNull String subject,
                                                            boolean verbose,
                                                            @NotNull SubjectSchemaRegistration schema) {
        return Mono.fromCallable(() -> api.testCompatibilityLatest(subject, verbose, schema));
    }

    @Override
    public void close() {
        this.api.close();
    }
}
