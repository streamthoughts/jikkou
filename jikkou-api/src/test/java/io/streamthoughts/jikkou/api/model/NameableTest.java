/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NameableTest {

    @Test
    void should_group_by_name() {
        // Given
        List<Nameable> nameables = List.of(Nameable.of("A"), Nameable.of("A"), Nameable.of("B"));

        // When
        Map<String, List<Nameable>> map = Nameable.groupByName(nameables);

        // Then
        Assertions.assertEquals(2, map.get("A").size());
        Assertions.assertEquals(1, map.get("B").size());
    }

    @Test
    void should_sort_by_name() {
        // Given
        List<Nameable> nameables = List.of(Nameable.of("C"), Nameable.of("B"), Nameable.of("A"));

        // When
        List<Nameable> list = Nameable.sortByName(nameables);

        // Then
        Assertions.assertEquals("A", list.get(0).getName());
        Assertions.assertEquals("B", list.get(1).getName());
        Assertions.assertEquals("C", list.get(2).getName());
    }

    @Test
    void should_key_by_name_given_no_duplicate() {
        // Given
        List<Nameable> nameables = List.of(Nameable.of("A"), Nameable.of("B"));

        // When
        Map<String, Nameable> map = Nameable.keyByName(nameables);

        // Then
        Assertions.assertEquals("A", map.get("A").getName());
        Assertions.assertEquals("B", map.get("B").getName());
    }

    @Test
    void should_fail_key_by_name_given_duplicates() {
        // Given
        List<Nameable> nameables = List.of(Nameable.of("A"), Nameable.of("A"));

        // When / Then
        Assertions.assertThrows(IllegalStateException.class, () -> Nameable.keyByName(nameables));
    }
}