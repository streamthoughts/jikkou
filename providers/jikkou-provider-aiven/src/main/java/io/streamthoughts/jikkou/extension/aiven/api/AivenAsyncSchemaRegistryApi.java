/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.extension.aiven.api.data.CompatibilityCheckResponse;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * AsyncSchemaRegistryApi implementation for Aiven.
 */
public final class AivenAsyncSchemaRegistryApi implements AsyncSchemaRegistryApi {

    private final AivenApiClient api;

    /**
     * Creates a new {@link AivenAsyncSchemaRegistryApi} instance.
     *
     * @param api the AivenApiClient
     */
    public AivenAsyncSchemaRegistryApi(final @NotNull AivenApiClient api) {
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<List<String>> listSubjects() {
        return Mono.fromCallable(() -> api.listSchemaRegistrySubjects().subjects());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<List<Integer>> deleteSubjectVersions(@NotNull String subject,
                                                     boolean permanent) {
        return Mono.fromCallable(() -> {
                    api.deleteSchemaRegistrySubject(subject);
                    return Collections.emptyList();
                }
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<SubjectSchemaId> registerSubjectVersion(@NotNull String subject,
                                                        @NotNull SubjectSchemaRegistration schema,
                                                        boolean normalize) {
        // Drop references - not supported through the Aiven's API.
        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(
                schema.schema(),
                schema.schemaType(),
                null
        );
        return Mono.fromCallable(
                () -> new SubjectSchemaId(api.registerSchemaRegistrySubjectVersion(subject, registration).version())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<SubjectSchemaVersion> getLatestSubjectSchema(@NotNull String subject) {
        return Mono.fromCallable(
                () -> api.getSchemaRegistryLatestSubjectVersion(subject).version()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityLevelObject> getGlobalCompatibility() {
        return Mono.fromCallable(
                () -> new CompatibilityLevelObject(api.getSchemaRegistryGlobalCompatibility().compatibilityLevel().name())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityLevelObject> getSubjectCompatibilityLevel(@NotNull String subject,
                                                                       boolean defaultToGlobal) {
        return Mono.fromCallable(
                () -> new CompatibilityLevelObject(api.getSchemaRegistrySubjectCompatibility(subject).compatibilityLevel().name())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityObject> updateSubjectCompatibilityLevel(@NotNull String subject,
                                                                     @NotNull CompatibilityObject compatibility) {
        return Mono.fromCallable(
                () -> {
                    api.updateSchemaRegistrySubjectCompatibility(subject, compatibility);
                    return new CompatibilityObject(compatibility.compatibility());
                }
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityObject> deleteSubjectCompatibilityLevel(@NotNull String subject) {
        throw new AivenApiClientException(
                "Deleting configuration for Schema Registry subject is not supported by " +
                        "the Aiven API (for more information: https://api.aiven.io/doc/)."
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityCheck> testCompatibility(@NotNull String subject,
                                                      String version,
                                                      boolean verbose,
                                                      @NotNull SubjectSchemaRegistration schema) {
        return Mono.fromCallable(
                () -> {
                    CompatibilityCheckResponse response = api.checkSchemaRegistryCompatibility(subject, version, schema);
                    return new CompatibilityCheck(
                            response.isCompatible(),
                            Optional.ofNullable(response.message()).map(List::of).orElse(Collections.emptyList())
                    );
                }
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Mono<CompatibilityCheck> testCompatibilityLatest(@NotNull String subject,
                                                            boolean verbose,
                                                            @NotNull SubjectSchemaRegistration schema) {
        return testCompatibility(subject, "latest", verbose, schema);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        api.close();
    }
}
