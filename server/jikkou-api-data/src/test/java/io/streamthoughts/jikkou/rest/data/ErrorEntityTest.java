/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorEntityTest {

    @Test
    void shouldCreateErrorEntityWithEmptyMessage() {
        Assertions.assertDoesNotThrow(() -> {
            new ErrorEntity(404,"not_found");
        });
    }

    @Test
    void shouldThrowNullPointerForErrorEntityWithEmptyErrorCode() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ErrorEntity(404,null, null, null);
        });
    }
}