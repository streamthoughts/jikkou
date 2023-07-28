/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.SchemaString;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchema;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectVersion;
import io.streamthoughts.jikkou.schema.registry.api.restclient.RestClientException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around the REST API {@link SchemaRegistryApi} to provide asynchronous methods.
 */
public final class AsyncSchemaRegistryApi implements AutoCloseable {

    private final SchemaRegistryApi api;

    /**
     * Creates a new {@link AsyncSchemaRegistryApi} instance.
     *
     * @param api the {@link SchemaRegistryApi} to delegate methods called.
     */
    public AsyncSchemaRegistryApi(final @NotNull SchemaRegistryApi api) {
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    /**
     * @see SchemaRegistryApi#listSubjects()
     */
    public CompletableFuture<List<String>> listSubjects() {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(api::listSubjects)
        );
    }

    /**
     * @see SchemaRegistryApi#deleteSubjectVersions(String, boolean)
     */
    public CompletableFuture<List<Integer>> deleteSubjectVersions(String subject, boolean permanent) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.deleteSubjectVersions(subject, permanent))
        );
    }

    /**
     * @see SchemaRegistryApi#getAllSubjectVersions(String)
     */
    public CompletableFuture<List<Integer>> getSubjectVersions(String subject) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.getAllSubjectVersions(subject))
        );
    }

    /**
     * @see SchemaRegistryApi#registerSchema(String, SubjectSchemaRegistration, boolean)
     */
    public CompletableFuture<SubjectSchemaId> registerSubjectVersion(String subject,
                                                                     SubjectSchemaRegistration schema,
                                                                     boolean normalize) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.registerSchema(subject, schema, normalize))
        );
    }

    /**
     * @see SchemaRegistryApi#checkSubjectVersion(String, SubjectSchemaRegistration, boolean)
     */
    public CompletableFuture<SubjectSchema> checkSubjectVersion(String subject,
                                                                SubjectSchemaRegistration schema,
                                                                boolean normalize) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(() -> api.checkSubjectVersion(subject, schema, normalize))
        );
    }

    /**
     * @see SchemaRegistryApi#getLatestSubjectSchema(String)
     */
    public CompletableFuture<SubjectSchema> getLatestSubjectSchema(String subject) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.getLatestSubjectSchema(subject))
        );
    }

    /**
     * @see SchemaRegistryApi#getSchemaByVersion(String, int)
     */
    public CompletableFuture<SubjectSchema> getSubjectSchemaByVersion(String subject, int version) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.getSchemaByVersion(subject, version))
        );
    }

    /**
     * @see SchemaRegistryApi#getSchemasTypes()
     */
    public CompletableFuture<List<String>> getSchemasTypes() {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(api::getSchemasTypes)
        );
    }

    /**
     * @see SchemaRegistryApi#getSchemasTypes()
     */
    public CompletableFuture<SchemaString> getSchemaById(String id) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.getSchemaById(id))
        );
    }

    /**
     * @see SchemaRegistryApi#getSchemaOnlyById(String)
     */
    public CompletableFuture<String> getSchemaOnlyById(String id) {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(() -> api.getSchemaOnlyById(id))
        );
    }

    /**
     * @see SchemaRegistryApi#getVersionSchemaById(String)
     */
    public CompletableFuture<List<SubjectVersion>> getSchemaVersionsById(String id) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(() -> api.getVersionSchemaById(id))
        );
    }


    /**
     * @see SchemaRegistryApi#getGlobalCompatibility()
     */
    public CompletableFuture<CompatibilityLevelObject> getGlobalCompatibility() {
        return handleClientErrorException(
            CompletableFuture.supplyAsync(api::getGlobalCompatibility)
        );
    }

    /**
     * @see SchemaRegistryApi#getConfigCompatibility(String, boolean)
     */
    public CompletableFuture<CompatibilityLevelObject> getConfigCompatibility(String subject, boolean defaultToGlobal) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(() -> api.getConfigCompatibility(subject, defaultToGlobal))
        );
    }

    /**
     * @see SchemaRegistryApi#updateConfigCompatibility(String, CompatibilityObject)
     */
    public CompletableFuture<CompatibilityObject> updateConfigCompatibility(String subject,
                                                                            CompatibilityObject compatibility) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(() -> api.updateConfigCompatibility(subject, compatibility))
        );
    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    public CompletableFuture<CompatibilityObject> deleteConfigCompatibility(String subject) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(() -> api.deleteConfigCompatibility(subject))
        );
    }

    /**
     * @see SchemaRegistryApi#deleteConfigCompatibility(String)
     */
    public CompletableFuture<CompatibilityCheck> testCompatibility(String subject,
                                                                   int version,
                                                                   boolean verbose,
                                                                   SubjectSchemaRegistration schema) {
        return handleClientErrorException(
                CompletableFuture.supplyAsync(
                        () -> api.testCompatibility(subject, version, verbose, schema))
        );
    }

    private static <T> CompletableFuture<T> handleClientErrorException(CompletableFuture<T> future) {
        return future.handle((r, e) -> {
            if (e != null) {
                if (e.getCause() != null &&
                    e instanceof CompletionException completionError) {
                    e = completionError.getCause();
                }

                if (e instanceof RestClientException clientError) {
                    ErrorResponse error = clientError.getResponseEntity(ErrorResponse.class);
                    throw new SchemaRegistryClientException(clientError.response().getStatus(), error);
                }
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(e);
            }
            return r;
        });
    }

    @Override
    public void close() {
        this.api.close();
    }
}
