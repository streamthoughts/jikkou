/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JikkouAnnotatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JikkouAnnotator annotator;
    private JCodeModel codeModel;

    @BeforeEach
    void setUp() {
        annotator = new JikkouAnnotator();
        codeModel = new JCodeModel();
    }

    @Test
    void shouldReturnFalseForAdditionalPropertiesSupported() {
        assertFalse(annotator.isAdditionalPropertiesSupported());
    }

    // --- propertyField tests ---

    @Test
    void shouldAnnotateArrayFieldWithSingular() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");
        JFieldVar field = clazz.field(JMod.PRIVATE, String.class, "items");

        ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.put("type", "array");

        annotator.propertyField(field, clazz, "items", propertyNode);

        assertTrue(hasAnnotation(field.annotations(), "lombok.Singular"));
    }

    @Test
    void shouldNotAnnotateNonArrayFieldWithSingular() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");
        JFieldVar field = clazz.field(JMod.PRIVATE, String.class, "name");

        ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.put("type", "string");

        annotator.propertyField(field, clazz, "name", propertyNode);

        assertFalse(hasAnnotation(field.annotations(), "lombok.Singular"));
    }

    @Test
    void shouldAnnotateFieldWithDefaultValue() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");
        JFieldVar field = clazz.field(JMod.PRIVATE, String.class, "status");

        ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.put("type", "string");
        propertyNode.put("default", "active");

        annotator.propertyField(field, clazz, "status", propertyNode);

        assertTrue(hasAnnotation(field.annotations(), "lombok.Builder.Default"));
    }

    @Test
    void shouldNotAnnotateFieldWithoutDefaultValue() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");
        JFieldVar field = clazz.field(JMod.PRIVATE, String.class, "status");

        ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.put("type", "string");

        annotator.propertyField(field, clazz, "status", propertyNode);

        assertFalse(hasAnnotation(field.annotations(), "lombok.Builder.Default"));
    }

    @Test
    void shouldAnnotateArrayFieldWithDefaultWithBothAnnotations() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");
        JFieldVar field = clazz.field(JMod.PRIVATE, String.class, "tags");

        ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.put("type", "array");
        propertyNode.putArray("default");

        annotator.propertyField(field, clazz, "tags", propertyNode);

        assertTrue(hasAnnotation(field.annotations(), "lombok.Singular"));
        assertTrue(hasAnnotation(field.annotations(), "lombok.Builder.Default"));
    }

    // --- propertyOrder tests ---

    @Test
    void shouldAnnotateClassWithApiVersionFromProperties() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestResource");

        ObjectNode propertiesNode = MAPPER.createObjectNode();
        ObjectNode apiVersionNode = MAPPER.createObjectNode();
        apiVersionNode.put("default", "v1beta1");
        propertiesNode.set("apiVersion", apiVersionNode);

        annotator.propertyOrder(clazz, propertiesNode);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.ApiVersion"));
        assertTrue(hasAnnotation(clazz.annotations(), "lombok.extern.jackson.Jacksonized"));
        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Reflectable"));
    }

    @Test
    void shouldAnnotateClassWithKindFromProperties() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestResource");

        ObjectNode propertiesNode = MAPPER.createObjectNode();
        ObjectNode kindNode = MAPPER.createObjectNode();
        kindNode.put("default", "KafkaTopic");
        propertiesNode.set("kind", kindNode);

        annotator.propertyOrder(clazz, propertiesNode);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Kind"));
    }

    @Test
    void shouldAlwaysAnnotateWithJacksonizedAndReflectable() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestResource");

        ObjectNode propertiesNode = MAPPER.createObjectNode();

        annotator.propertyOrder(clazz, propertiesNode);

        assertTrue(hasAnnotation(clazz.annotations(), "lombok.extern.jackson.Jacksonized"));
        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Reflectable"));
    }

    @Test
    void shouldNotAnnotateWithApiVersionWhenNoDefaultPresent() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestResource");

        ObjectNode propertiesNode = MAPPER.createObjectNode();
        ObjectNode apiVersionNode = MAPPER.createObjectNode();
        apiVersionNode.put("type", "string"); // no "default" field
        propertiesNode.set("apiVersion", apiVersionNode);

        annotator.propertyOrder(clazz, propertiesNode);

        assertFalse(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.ApiVersion"));
    }

    // --- propertyInclusion tests ---

    @Test
    void shouldAddDefaultAnnotationsWhenNoAdditionalProperties() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        // no "additionalProperties" key

        annotator.propertyInclusion(clazz, schema);

        // Default annotations should be added (all enabledByDefault=false in JikkouAnnotator,
        // so no lombok annotations should be added by default)
        // The method calls addDefaultAnnotations which adds all enabled-by-default annotations
        // Since all are enabledByDefault=false, no lombok annotations should be present
        Set<String> annotationNames = getAnnotationNames(clazz.annotations());
        assertFalse(annotationNames.contains("lombok.Getter"));
        assertFalse(annotationNames.contains("lombok.Setter"));
    }

    @Test
    void shouldAddLombokGetterWhenExplicitlyEnabled() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "lombok.Getter"));
    }

    @Test
    void shouldNotAddLombokGetterWhenExplicitlyDisabled() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", false);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertFalse(hasAnnotation(clazz.annotations(), "lombok.Getter"));
    }

    @Test
    void shouldAddMultipleLombokAnnotations() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);
        additionalProperties.put("lombok-setter", true);
        additionalProperties.put("lombok-builder", true);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "lombok.Getter"));
        assertTrue(hasAnnotation(clazz.annotations(), "lombok.Setter"));
        assertTrue(hasAnnotation(clazz.annotations(), "lombok.Builder"));
    }

    @Test
    void shouldAddDescriptionAnnotation() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);
        schema.set("additionalProperties", additionalProperties);
        schema.put("description", "A test resource description");

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Description"));
        assertTrue(hasAnnotation(clazz.annotations(), "com.fasterxml.jackson.annotation.JsonClassDescription"));
    }

    @Test
    void shouldAddNamesAnnotationFromSpec() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);

        ObjectNode spec = MAPPER.createObjectNode();
        ObjectNode names = MAPPER.createObjectNode();
        names.put("singular", "topic");
        names.put("plural", "topics");
        names.putArray("shortNames").add("kt");
        spec.set("names", names);
        additionalProperties.set("spec", spec);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Names"));
        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Verbs"));
    }

    @Test
    void shouldPropagateLocalNameFromSpecNamesToNamesAnnotation() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);

        ObjectNode spec = MAPPER.createObjectNode();
        ObjectNode names = MAPPER.createObjectNode();
        names.put("singular", "topic");
        names.put("plural", "topics");
        names.put("local", "topics");
        spec.set("names", names);
        additionalProperties.set("spec", spec);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Names"));
    }

    @Test
    void shouldAddVerbsAnnotationFromSpec() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);

        ObjectNode spec = MAPPER.createObjectNode();
        spec.putArray("verbs").add("get").add("list").add("create");
        additionalProperties.set("spec", spec);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Verbs"));
    }

    @Test
    void shouldAddTransientAnnotationFromSpec() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);

        ObjectNode spec = MAPPER.createObjectNode();
        spec.put("isTransient", true);
        additionalProperties.set("spec", spec);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertTrue(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Transient"));
    }

    @Test
    void shouldNotAddTransientAnnotationWhenNotInSpec() throws Exception {
        JDefinedClass clazz = codeModel._class("com.example.TestClass");

        ObjectNode schema = MAPPER.createObjectNode();
        ObjectNode additionalProperties = MAPPER.createObjectNode();
        additionalProperties.put("lombok-getter", true);

        ObjectNode spec = MAPPER.createObjectNode();
        additionalProperties.set("spec", spec);
        schema.set("additionalProperties", additionalProperties);

        annotator.propertyInclusion(clazz, schema);

        assertFalse(hasAnnotation(clazz.annotations(), "io.jikkou.core.annotation.Transient"));
    }

    // --- Helper methods ---

    private static boolean hasAnnotation(Collection<JAnnotationUse> annotations, String annotationFqn) {
        return getAnnotationNames(annotations).contains(annotationFqn);
    }

    private static Set<String> getAnnotationNames(Collection<JAnnotationUse> annotations) {
        return annotations.stream()
                .map(a -> a.getAnnotationClass().fullName())
                .collect(Collectors.toSet());
    }
}
