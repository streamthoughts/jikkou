/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SecurePasswordGeneratorTest {

    @Test
    void shouldGeneratePassword() {
        String generated = SecurePasswordGenerator.getDefault().generate(32);
        Assertions.assertNotNull(generated);
        Assertions.assertEquals(32, generated.length());
    }
}