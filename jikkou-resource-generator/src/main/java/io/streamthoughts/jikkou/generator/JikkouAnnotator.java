/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import io.streamthoughts.jikkou.api.model.annotations.ApiVersion;
import io.streamthoughts.jikkou.api.model.annotations.Description;
import io.streamthoughts.jikkou.api.model.annotations.Kind;
import io.streamthoughts.jikkou.api.model.annotations.Names;
import io.streamthoughts.jikkou.api.model.annotations.Transient;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.jsonschema2pojo.AbstractAnnotator;

public class JikkouAnnotator extends AbstractAnnotator {

    private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    private static final String LOMBOK_SCHEMA_PROPERTY_PREFIX = "lombok-";
    private static final String API_VERSION_JSON_SCHEMA_FIELD = "apiVersion";
    private static final String KIND_JSON_SCHEMA_VERSION = "kind";
    private static final String DEFAULT_JSON_SCHEMA_FIELD = "default";
    public static final String SPEC_NAMES_SINGULAR = "singular";
    public static final String SPEC_NAMES_PLURAL = "plural";
    public static final String SPEC_NAMES = "names";
    public static final String SPEC_NAMES_SHORT_NAMES = "shortNames";
    public static final String SPEC = "spec";
    public static final String SPEC_IS_TRANSIENT = "isTransient";

    private final Mapping classAnnotationMapping = new Mapping();

    /**
     * Creates a new {@link JikkouAnnotator} instance.
     */
    public JikkouAnnotator() {
        registerAllLombokAnnotations(classAnnotationMapping);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyField(JFieldVar field,
                              JDefinedClass clazz,
                              String propertyName,
                              JsonNode propertyNode) {
        String type = propertyNode.get("type").asText();
        if (type.equalsIgnoreCase("array")) {
            field.annotate(Singular.class);
        }

        if (propertyNode.get("default") != null) {
            field.annotate(Builder.Default.class);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        var apiVersion = propertiesNode.get(API_VERSION_JSON_SCHEMA_FIELD);
        Optional.ofNullable(apiVersion)
                .flatMap(node -> Optional.ofNullable(node.get(DEFAULT_JSON_SCHEMA_FIELD)))
                .ifPresent(val -> {
                    JAnnotationUse annotate = clazz.annotate(ApiVersion.class);
                    annotate.param("value", val.textValue());
                });
        var kind = propertiesNode.get(KIND_JSON_SCHEMA_VERSION);
        Optional.ofNullable(kind)
                .flatMap(node -> Optional.ofNullable(node.get(DEFAULT_JSON_SCHEMA_FIELD)))
                .ifPresent(val -> {
                    JAnnotationUse annotate = clazz.annotate(Kind.class);
                    annotate.param("value", val.textValue());
                });
        clazz.annotate(Jacksonized.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        JsonNode additionalProperties = schema.get(ADDITIONAL_PROPERTIES);
        if (additionalProperties == null) {
            addDefaultAnnotations(clazz);
            return;
        }

        List<String> processedAnnotations = new ArrayList<>();
        additionalProperties.fieldNames().forEachRemaining(property -> {
            Optional<LombokAnnotation> annotation = classAnnotationMapping.getAnnotationFor(property);
            annotation.ifPresent(a -> {
                if (additionalProperties.get(property).asBoolean()) {
                    addAnnotationToClass(clazz, a);
                }
                processedAnnotations.add(property);
            });
        });

        if (processedAnnotations.isEmpty()) {
            addDefaultAnnotations(clazz);
        } else {
            classAnnotationMapping.getAllEnabledByDefault()
                    .stream()
                    .filter(a -> !processedAnnotations.contains(a.getSchemaProperty()))
                    .forEach(annotation -> addAnnotationToClass(clazz, annotation));
        }

        JsonNode descriptionProperty = schema.get("description");
        if (descriptionProperty != null) {
            JAnnotationUse annotate = clazz.annotate(Description.class);
            annotate.param("value", descriptionProperty.textValue());
        }


        JsonNode spec = additionalProperties.get(SPEC);
        if (spec != null) {
            JsonNode names = spec.get(SPEC_NAMES);
            if (names != null) {
                JAnnotationUse annotate = clazz.annotate(Names.class);
                if (names.has(SPEC_NAMES_SINGULAR)) {
                    annotate.param(SPEC_NAMES_SINGULAR, names.get(SPEC_NAMES_SINGULAR).textValue());
                }
                if (names.has(SPEC_NAMES_PLURAL)) {
                    annotate.param(SPEC_NAMES_PLURAL, names.get(SPEC_NAMES_PLURAL).textValue());
                }
                JsonNode shortNamesNode = names.get(SPEC_NAMES_SHORT_NAMES);
                if (shortNamesNode != null && shortNamesNode.isArray()) {
                    JAnnotationArrayMember params = annotate.paramArray(SPEC_NAMES_SHORT_NAMES);
                    StreamSupport.stream(shortNamesNode.spliterator(), false)
                            .map(JsonNode::textValue)
                            .forEach(params::param);
                }
            }
            JsonNode isTransient = spec.get(SPEC_IS_TRANSIENT);
            if (isTransient != null && isTransient.isBoolean()) {
                clazz.annotate(Transient.class);
            }
        }
    }

    private void addDefaultAnnotations(JDefinedClass clazz) {
        classAnnotationMapping.getAllEnabledByDefault()
                .forEach(annotation -> addAnnotationToClass(clazz, annotation));
    }

    private static void addAnnotationToClass(JDefinedClass clazz, LombokAnnotation annotation) {
        JAnnotationUse annotate = clazz.annotate(annotation.getAnnotation());
        if (annotation.getParams() != null) {
            annotation.getParams().forEach(a -> a.accept(annotate));
        }
    }

    public boolean isAdditionalPropertiesSupported() {
        return false;
    }

    @With
    @AllArgsConstructor
    @Builder
    @Getter
    private static final class LombokAnnotation {
        private final String schemaProperty;
        private final Class<? extends Annotation> annotation;
        @Singular
        private final List<AnnotationParam> params;
        private final boolean enabledByDefault;

    }

    private interface AnnotationParam extends Consumer<JAnnotationUse> {

    }

    private static final class Mapping {

        private final Map<String, LombokAnnotation> annotations;

        public Mapping() {
            this.annotations = new LinkedHashMap<>();
        }

        private Mapping registerLombokAnnotation(final LombokAnnotation annotation) {
            this.annotations.put(annotation.getSchemaProperty(), annotation);
            return this;
        }

        private Optional<LombokAnnotation> getAnnotationFor(final String schemaProperty) {
            return Optional.ofNullable(annotations.get(schemaProperty));
        }

        public List<LombokAnnotation> getAllEnabledByDefault() {
            return annotations.values()
                    .stream()
                    .filter(LombokAnnotation::isEnabledByDefault)
                    .toList();
        }
    }

    private static void registerAllLombokAnnotations(final Mapping annotations) {
        annotations
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "no-args-constructor")
                                .annotation(NoArgsConstructor.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "all-args-constructor")
                                .annotation(AllArgsConstructor.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "to-string")
                                .annotation(ToString.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "setter")
                                .annotation(Setter.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "getter")
                                .annotation(Getter.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "equals-and-hash-code")
                                .annotation(EqualsAndHashCode.class)
                                .enabledByDefault(false)
                                .param(a -> a.param("doNotUseGetters", true))
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "with")
                                .annotation(With.class)
                                .enabledByDefault(false)
                                .build()
                )
                .registerLombokAnnotation(
                        LombokAnnotation.builder()
                                .schemaProperty(LOMBOK_SCHEMA_PROPERTY_PREFIX + "builder")
                                .annotation(Builder.class)
                                .enabledByDefault(false)
                                .param(a -> a.param("builderMethodName", "builder"))
                                .param(a -> a.param("toBuilder", true))
                                .param(a -> a.param("setterPrefix", "with"))
                                .build()
                );
    }
}
