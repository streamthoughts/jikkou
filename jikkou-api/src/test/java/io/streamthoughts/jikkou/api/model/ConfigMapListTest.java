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

import io.streamthoughts.jikkou.api.models.ConfigMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigMapListTest {

    public static final String TEST_NAME_CONFIG = "config";

    @Test
    void should_return_config_given_existing_name() {
        // Given
        ConfigMap configMap = new ConfigMap().toBuilder()
                .withMetadata(new ObjectMeta().withName(TEST_NAME_CONFIG))
                .build();
        ConfigMapList list = new ConfigMapList(List.of(configMap));

        // When
        Optional<ConfigMap> result = list.findByName(TEST_NAME_CONFIG);

        // Then
        Assertions.assertTrue(list.containsConfigMap(TEST_NAME_CONFIG));
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void should_not_return_config_given_non_existing_name() {
        // Given
        ConfigMapList list = new ConfigMapList(Collections.emptyList());

        // When
        Optional<ConfigMap> result = list.findByName(TEST_NAME_CONFIG);

        // Then
        Assertions.assertFalse(list.containsConfigMap(TEST_NAME_CONFIG));
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void should_return_illegal_exception_given_duplicate_configs() {
        // Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            // Given
            ConfigMap configMap = new ConfigMap().toBuilder()
                    .withMetadata(new ObjectMeta().withName(TEST_NAME_CONFIG))
                    .build();
            // When
             new ConfigMapList(List.of(configMap, configMap));
        });
    }
}