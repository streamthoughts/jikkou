/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeprecatedClassNameResolverTest {

    @Test
    void shouldResolveOldPrefixToNewPrefix() {
        String result = DeprecatedClassNameResolver.resolve(
            "io.streamthoughts.jikkou.kafka.validation.TopicNameRegexValidation");
        assertEquals("io.jikkou.kafka.validation.TopicNameRegexValidation", result);
    }

    @Test
    void shouldReturnUnchangedWhenNewPrefix() {
        String input = "io.jikkou.kafka.validation.TopicNameRegexValidation";
        assertEquals(input, DeprecatedClassNameResolver.resolve(input));
    }

    @Test
    void shouldReturnUnchangedWhenUnrelatedPackage() {
        String input = "com.example.MyExtension";
        assertEquals(input, DeprecatedClassNameResolver.resolve(input));
    }

    @Test
    void shouldReturnNullWhenNull() {
        assertNull(DeprecatedClassNameResolver.resolve(null));
    }

    @Test
    void shouldDetectDeprecatedClassName() {
        assertTrue(DeprecatedClassNameResolver.isDeprecated(
            "io.streamthoughts.jikkou.core.CoreExtensionProvider"));
        assertFalse(DeprecatedClassNameResolver.isDeprecated(
            "io.jikkou.core.CoreExtensionProvider"));
        assertFalse(DeprecatedClassNameResolver.isDeprecated(null));
    }

    @Test
    void shouldConvertToDeprecatedName() {
        assertEquals(
            "io.streamthoughts.jikkou.kafka.KafkaExtensionProvider",
            DeprecatedClassNameResolver.toDeprecatedName("io.jikkou.kafka.KafkaExtensionProvider"));
    }

    @Test
    void shouldReturnUnchangedWhenNotNewPrefix() {
        String input = "com.example.MyClass";
        assertEquals(input, DeprecatedClassNameResolver.toDeprecatedName(input));
    }
}
