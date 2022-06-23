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
package io.streamthoughts.jikkou.kafka.control.change;

import static io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig.DELETE_CONFIG_ORPHANS_OPTION;
import static io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig.DELETE_TOPIC_ORPHANS_OPTION;
import static io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig.EXCLUDE_INTERNAL_TOPICS_OPTION;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicReconciliationConfigTest {

    @Test
    void should_set_options_given_empty_generic_options() {
        // Given
        var config = new KafkaTopicReconciliationConfig();

        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(DELETE_CONFIG_ORPHANS_OPTION.defaultValueSupplier().get(), config.isDeleteConfigOrphans());
        Assertions.assertEquals(DELETE_TOPIC_ORPHANS_OPTION.defaultValueSupplier().get(), config.isDeleteTopicOrphans());
        Assertions.assertEquals(EXCLUDE_INTERNAL_TOPICS_OPTION.defaultValueSupplier().get(), config.isExcludeInternalTopics());
    }
}