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
package io.streamthoughts.jikkou.kafka.transformations;

import io.streamthoughts.jikkou.api.error.InvalidResourceException;
import io.streamthoughts.jikkou.api.extensions.annotations.EnableAutoConfigure;
import io.streamthoughts.jikkou.api.model.ConfigMapList;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.HashMap;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Transformation to apply all config-maps to topic objects.
 */
@EnableAutoConfigure
public class TopicConfigMapsTransformation extends TopicTransformation {

    /** {@inheritDoc */
    @Override
    public @NotNull V1KafkaTopicObject transformTopic(@NotNull V1KafkaTopicObject topic,
                                                      @NotNull ResourceList list) {

        var configMapRefs = topic.getConfigMapRefs();

        if (configMapRefs == null || configMapRefs.isEmpty()) {
            return topic;
        }

        var configMapList = new ConfigMapList(list.getAllResourcesForClass(ConfigMap.class));

        var newConfigs = new HashMap<String, Object>();

        for (String configMapRef : configMapRefs) {
            ConfigMap configMap = configMapList.findByName(configMapRef)
                    .orElseThrow(() -> new InvalidResourceException(
                            "Unknown ConfigMap for name '" + configMapRef + "'"
                    ));
            Optional.ofNullable(configMap.getData())
                    .ifPresent(config -> newConfigs.putAll(config.toMap()));
        }

        Optional.ofNullable(topic.getConfigs())
                .ifPresent(config -> newConfigs.putAll(config.toMap()));

        return topic.withConfigs(Configs.of(newConfigs));
    }
}
