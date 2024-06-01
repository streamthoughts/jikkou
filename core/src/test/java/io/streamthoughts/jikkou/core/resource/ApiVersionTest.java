/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiVersionTest {

    @Test
    void shouldParseGivenVersion() {
        ApiVersion version = ApiVersion.of("v1");
        Assertions.assertEquals(1, version.version());
        Assertions.assertNull(version.qualifier());
    }

    @Test
    void shouldParseGivenVersionAndQualifier() {
        ApiVersion version = ApiVersion.of("v1beta2");
        Assertions.assertEquals(1, version.version());
        Assertions.assertEquals("beta", version.qualifier().label());
        Assertions.assertEquals(2, version.qualifier().number());
    }

    @Test
    void shouldGetLatestVersion() {
        ApiVersion latest = ApiVersion.getLatest(
            ApiVersion.of("v1alpha1"),
            ApiVersion.of("v1"),
            ApiVersion.of("v1beta2"),
            ApiVersion.of("v2")
        );
        Assertions.assertEquals(ApiVersion.of("v2"), latest);
    }
}