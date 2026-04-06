/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoreAnnotationsTest {

    @Test
    void shouldReturnProviderAnnotationValue() {
        HasMetadata resource = newResourceWithAnnotation(
                CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod");

        String provider = CoreAnnotations.getProvider(resource);

        Assertions.assertEquals("kafka-prod", provider);
    }

    @Test
    void shouldReturnNullWhenNoProviderAnnotation() {
        HasMetadata resource = newResource("test-resource");

        String provider = CoreAnnotations.getProvider(resource);

        Assertions.assertNull(provider);
    }

    @Test
    void shouldReturnNullWhenResourceHasNoMetadata() {
        HasMetadata resource = new TestResource();

        String provider = CoreAnnotations.getProvider(resource);

        Assertions.assertNull(provider);
    }

    @Test
    void shouldMatchProviderWhenAnnotationMatches() {
        HasMetadata resource = newResourceWithAnnotation(
                CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-prod");

        Assertions.assertTrue(CoreAnnotations.matchesProvider(resource, "kafka-prod"));
    }

    @Test
    void shouldNotMatchProviderWhenAnnotationDiffers() {
        HasMetadata resource = newResourceWithAnnotation(
                CoreAnnotations.JIKKOU_IO_PROVIDER, "kafka-staging");

        Assertions.assertFalse(CoreAnnotations.matchesProvider(resource, "kafka-prod"));
    }

    @Test
    void shouldMatchProviderWhenNoAnnotationPresent() {
        HasMetadata resource = newResource("test-resource");

        Assertions.assertTrue(CoreAnnotations.matchesProvider(resource, "kafka-prod"));
    }

    @Test
    void shouldDetectIgnoreAnnotation() {
        HasMetadata resource = newResourceWithAnnotation(
                CoreAnnotations.JIKKOU_IO_IGNORE, true);

        Assertions.assertTrue(CoreAnnotations.isAnnotatedWithIgnore(resource));
    }

    @Test
    void shouldDetectDeleteAnnotation() {
        HasMetadata resource = newResourceWithAnnotation(
                CoreAnnotations.JIKKOU_IO_DELETE, true);

        Assertions.assertTrue(CoreAnnotations.isAnnotatedWithDelete(resource));
    }

    @Test
    void shouldReturnFalseWhenAnnotationNotPresent() {
        HasMetadata resource = newResource("test-resource");

        Assertions.assertFalse(CoreAnnotations.isAnnotatedWithIgnore(resource));
        Assertions.assertFalse(CoreAnnotations.isAnnotatedWithDelete(resource));
        Assertions.assertFalse(CoreAnnotations.isAnnotatedWithReplace(resource));
    }

    private static HasMetadata newResource(String name) {
        return new TestResource().withMetadata(new ObjectMeta(name));
    }

    private static HasMetadata newResourceWithAnnotation(String key, Object value) {
        return new TestResource().withMetadata(
                new ObjectMeta("test-resource", null, Map.of(key, value)));
    }

    private static class TestResource implements HasMetadata {
        private ObjectMeta meta;

        @Override
        public ObjectMeta getMetadata() {
            return meta;
        }

        @Override
        public HasMetadata withMetadata(ObjectMeta metadata) {
            this.meta = metadata;
            return this;
        }
    }
}
