/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.transforms;

import io.streamthoughts.kafka.specs.model.*;
import io.streamthoughts.kafka.specs.resources.Configs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplyConfigMapsTransformationTest {

    public static final String TEST_CONFIG_MAP_NAME = "configMap";

    private final static V1ConfigMap TEST_CONFIG_MAP = new V1ConfigMap(TEST_CONFIG_MAP_NAME, Map.of("k1", "v1"));

    private final static V1TopicObject TEST_TOPIC_OBJECT = new V1TopicObject(
            "topic",
            null,
            null,
            Configs.empty(),
            Set.of(TEST_CONFIG_MAP_NAME)
    );

    @Test
    public void should_add_config_props_to_topic_given_valid_config_map() {
        V1SpecsObject object = new V1SpecsObject()
                .topics(List.of(TEST_TOPIC_OBJECT))
                .configMaps(List.of(TEST_CONFIG_MAP));

        V1SpecsObject transformed = new ApplyConfigMapsTransformation().transform(object);
        V1TopicObject v1TopicObject = transformed.topics().get(0);
        Assertions.assertEquals("v1", v1TopicObject.configs().get("k1").value());
    }
}