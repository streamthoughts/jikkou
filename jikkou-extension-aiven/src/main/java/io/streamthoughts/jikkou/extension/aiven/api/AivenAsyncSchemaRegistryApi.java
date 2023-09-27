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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.extension.aiven.api.data.CompatibilityCheckResponse;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaVersion;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

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
    public CompletableFuture<List<String>> listSubjects() {
        return CompletableFuture.supplyAsync(
                () -> api.listSchemaRegistrySubjects().subjects()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<List<Integer>> deleteSubjectVersions(@NotNull String subject,
                                                                  boolean permanent) {
        return CompletableFuture.supplyAsync(
                () -> {
                    api.deleteSchemaRegistrySubject(subject);
                    return Collections.emptyList();
                }
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<SubjectSchemaId> registerSubjectVersion(@NotNull String subject,
                                                                     @NotNull SubjectSchemaRegistration schema,
                                                                     boolean normalize) {
        // Drop references - not supported through the Aiven's API.
        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(
                schema.schema(),
                schema.schemaType(),
                null
        );
        return CompletableFuture.supplyAsync(
                () -> new SubjectSchemaId(api.registerSchemaRegistrySubjectVersion(subject, registration).version())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<SubjectSchemaVersion> getLatestSubjectSchema(@NotNull String subject) {
        return CompletableFuture.supplyAsync(
                () -> api.getSchemaRegistryLatestSubjectVersion(subject).version()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<CompatibilityLevelObject> getGlobalCompatibility() {
        return CompletableFuture.supplyAsync(
                () -> new CompatibilityLevelObject(api.getSchemaRegistryGlobalCompatibility().compatibilityLevel().name())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<CompatibilityLevelObject> getSubjectCompatibilityLevel(@NotNull String subject,
                                                                                    boolean defaultToGlobal) {
        return CompletableFuture.supplyAsync(
                () -> new CompatibilityLevelObject(api.getSchemaRegistrySubjectCompatibility(subject).compatibilityLevel().name())
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<CompatibilityObject> updateSubjectCompatibilityLevel(@NotNull String subject,
                                                                                  @NotNull CompatibilityObject compatibility) {
        return CompletableFuture.supplyAsync(
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
    public CompletableFuture<CompatibilityObject> deleteSubjectCompatibilityLevel(@NotNull String subject) {
        throw new AivenApiClientException(
                "Deleting configuration for Schema Registry subject is not supported by " +
                        "the Aiven API (for more information: https://api.aiven.io/doc/)."
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public CompletableFuture<CompatibilityCheck> testCompatibility(@NotNull String subject,
                                                                   String version,
                                                                   boolean verbose,
                                                                   @NotNull SubjectSchemaRegistration schema) {
        return CompletableFuture.supplyAsync(
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
    public CompletableFuture<CompatibilityCheck> testCompatibilityLatest(@NotNull String subject,
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
