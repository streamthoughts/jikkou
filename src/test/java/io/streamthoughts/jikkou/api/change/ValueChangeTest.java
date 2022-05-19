/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.api.change;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValueChangeTest {

    @Test
    public void testShouldReturnDeleteOperation() {
        // Given - When
        ValueChange<String> value = ValueChange.withBeforeValue("dummy");

        // Then
        Assertions.assertEquals(Change.OperationType.DELETE, value.getOperation());
    }

    @Test
    public void testShouldReturnUpdateOperation() {
        // Given - When
        ValueChange<String> value = ValueChange.with("foo", "bar");

        // Then
        Assertions.assertEquals(Change.OperationType.UPDATE, value.getOperation());
    }

    @Test
    public void testShouldReturnAddOperation() {
        // Given - When
        ValueChange<String> value = ValueChange.withAfterValue("dummy");

        // Then
        Assertions.assertEquals(Change.OperationType.ADD, value.getOperation());
    }

    @Test
    public void testShouldReturnNoneOperation() {
        // Given - When
        ValueChange<String> value = ValueChange.with("dummy", "dummy");

        // Then
        Assertions.assertEquals(Change.OperationType.NONE, value.getOperation());
    }
}