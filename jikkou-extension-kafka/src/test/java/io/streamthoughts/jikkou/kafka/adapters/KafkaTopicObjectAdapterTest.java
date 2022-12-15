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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.kafka.internals.KafkaConstants;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicObjectAdapterTest {

    private final KafkaTopicObjectAdapter adapter = new KafkaTopicObjectAdapter(
            V1KafkaTopicObject
                    .builder()
                    .withName("test")
                    .build()
    );

    @Test
    void should_get_default_partition_given_null() {
        Assertions.assertEquals(adapter.getPartitionsOrDefault(), KafkaConstants.NO_NUM_PARTITIONS);
    }

    @Test
    void should_get_default_replication_factor_given_null() {
        Assertions.assertEquals(adapter.getReplicationFactorOrDefault(), KafkaConstants.NO_REPLICATION_FACTOR);
    }

    @Test
    void should_get_default_config_given_null() {
        Assertions.assertEquals(0, adapter.getConfigs().size());
    }
}