/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Transient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HasMetadataTest {

    @Test
    void shouldGetApiVersion() {
        String apiVersion = Resource.getApiVersion(TransientTestResource.class);
        Assertions.assertEquals("version", apiVersion);
    }

    @Test
    void shouldGetKind() {
        String apiVersion = Resource.getKind(TransientTestResource.class);
        Assertions.assertEquals("kind", apiVersion);
    }

    @Test
    void shouldGetTrueForTransientResource() {
        boolean isTransient = Resource.isTransient(TransientTestResource.class);
        Assertions.assertTrue(isTransient);
    }

    @Test
    void shouldGetFalseForNonTransientResource() {
        boolean isTransient = Resource.isTransient(NonTransientTestResource.class);
        Assertions.assertFalse(isTransient);
    }

    private static class TestResource implements HasMetadata {

        @Override
        public ObjectMeta getMetadata() {
            return null;
        }

        @Override
        public HasMetadata withMetadata(ObjectMeta metadata) {
            return null;
        }

        @Override
        public String getApiVersion() {
            return null;
        }

        @Override
        public String getKind() {
            return null;
        }
    }
    @ApiVersion("version")
    @Kind("kind")
    static class NonTransientTestResource extends TestResource {}

    @ApiVersion("version")
    @Kind("kind")
    @Transient
    static class TransientTestResource extends TestResource {}
}