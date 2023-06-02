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
package io.streamthoughts.jikkou.schema.registry.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientException;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchema;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.SchemaRegistry;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistryCollector implements ResourceCollector<V1SchemaRegistrySubject> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryCollector.class);

    private SchemaRegistryClientConfig config;

    private AsyncSchemaRegistryApi api;

    private boolean prettyPrintSchema = true;

    private boolean defaultToGlobalCompatibilityLevel = true;

    /**
     * Creates a new {@link SchemaRegistryCollector} instance.
     */
    public SchemaRegistryCollector() {}

    /**
     * Creates a new {@link SchemaRegistryCollector} instance.
     *
     * @param config the configuration.
     */
    public SchemaRegistryCollector(SchemaRegistryClientConfig config) {
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
        this.api = new AsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(config));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<V1SchemaRegistrySubject> listAll(@NotNull Configuration configuration,
                                                 @NotNull List<ResourceSelector> selectors) {


        CompletableFuture<List<V1SchemaRegistrySubject>> result = api
                .listSubjects()
                .thenComposeAsync(subjects -> AsyncUtils.waitForAll(getAllSubjects(subjects)));
        Optional<Throwable> exception = AsyncUtils.getException(result);
        if (exception.isPresent()) {
            throw new JikkouRuntimeException("Failed to list all subject schemas", exception.get());
        }
        return result.join();
    }

    public SchemaRegistryCollector prettyPrintSchema(final boolean prettyPrintSchema) {
        this.prettyPrintSchema = prettyPrintSchema;
        return this;
    }

    public SchemaRegistryCollector defaultToGlobalCompatibilityLevel(final boolean defaultToGlobalCompatibilityLevel) {
        this.defaultToGlobalCompatibilityLevel = defaultToGlobalCompatibilityLevel;
        return this;
    }

    @NotNull
    private List<CompletableFuture<V1SchemaRegistrySubject>> getAllSubjects(List<String> subjects) {
        return subjects.stream().map(subject -> api
                .getLatestSubjectSchema(subject)
                .thenCombine(api
                            .getConfigCompatibility(subject, defaultToGlobalCompatibilityLevel)
                            .thenApply(compatibilityObject -> CompatibilityLevels.valueOf(compatibilityObject.compatibilityLevel()))
                            .exceptionally(t -> {
                                if (t.getCause() != null) t = t.getCause();

                                if (t instanceof SchemaRegistryClientException registryError) {
                                    if (registryError.getResponseCode() == 404)
                                        return null;
                                }
                                if (t instanceof RuntimeException re) {
                                    throw re;
                                }
                                throw new JikkouRuntimeException(t);
                            })
                        ,
                        this::mapToV1SchemaRegistrySubject
                )
        ).toList();
    }
    
    @NotNull
    private V1SchemaRegistrySubject mapToV1SchemaRegistrySubject(SubjectSchema subjectSchema,
                                                                 CompatibilityLevels compatibilityLevels) {
        SchemaType schemaType = Optional.ofNullable(subjectSchema.schemaType())
                .map(SchemaType::getForNameIgnoreCase)
                .orElse(SchemaType.defaultType());

        V1SchemaRegistrySubjectSpec.V1SchemaRegistrySubjectSpecBuilder specBuilder = V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaRegistry(SchemaRegistry
                        .builder()
                        .withVendor(config.getSchemaRegistryVendor())
                        .build()
                )
                .withSchemaType(schemaType)
                .withSchema(new SchemaHandle(subjectSchema.schema()));

        if (compatibilityLevels != null) {
            specBuilder = specBuilder.withCompatibilityLevel(compatibilityLevels);
        }

        V1SchemaRegistrySubject res = new V1SchemaRegistrySubject()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(subjectSchema.subject())
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_URL,
                                config.getSchemaRegistryUrl())
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_VERSION,
                                subjectSchema.version())
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID,
                                subjectSchema.id())
                        .build()
                )
                .withSpec(specBuilder.build())
                .build();

        if (prettyPrintSchema) {
            return prettyPrintSchema(res);
        }

        return res;
    }

    @NotNull
    private V1SchemaRegistrySubject prettyPrintSchema(@NotNull V1SchemaRegistrySubject resource) {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();
        SchemaType type = spec.getSchemaType();
        if (type == SchemaType.AVRO || type == SchemaType.JSON) {
            SchemaHandle schema = spec.getSchema();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode tree = objectMapper.readTree(schema.value());
                String pretty = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(tree);
                spec.setSchema(new SchemaHandle(pretty));

            } catch (JsonProcessingException e) {
                LOG.warn("Failed to parse AVRO or JSON schema", e);
            }
        }
        return resource;
    }
}
