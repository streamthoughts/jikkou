/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataValueTest {

    @Test
    void shouldNotReturnNullFromData() {
        // Given
        DataValue value = new DataValue(DataType.STRING, null);
        // When/Then
        Assertions.assertEquals(DataHandle.NULL, value.data());
    }

}
