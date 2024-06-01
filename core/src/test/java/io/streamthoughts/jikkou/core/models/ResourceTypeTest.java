/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {

    private static final String GROUP = "io.jikkou.test";
    private static final String KIND = "Test";
    private static final String VERSION = "v1";

    @Test
    void shouldCreateResourceTypeGivenKindAndGroup() {
        ResourceType type = ResourceType.of(KIND, GROUP);
        Assertions.assertEquals(new ResourceType(KIND, GROUP, null, false), type);
    }

    @Test
    void shouldCreateResourceTypeGivenKindAndApiVersion() {
        ResourceType type = ResourceType.of(KIND, GROUP + "/" + VERSION);
        Assertions.assertEquals(new ResourceType(KIND, GROUP, VERSION, false), type);
    }
}