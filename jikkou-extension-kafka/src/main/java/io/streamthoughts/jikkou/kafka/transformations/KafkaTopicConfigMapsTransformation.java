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

import io.streamthoughts.jikkou.api.annotations.Priority;
import io.streamthoughts.jikkou.api.error.InvalidResourceException;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasPriority;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.api.models.ConfigMapList;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.HashMap;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This transformation is used to apply all config-maps to topic objects.
 *
 * @see ConfigMap
 */
@Priority(HasPriority.LOWEST_PRECEDENCE)
public class KafkaTopicConfigMapsTransformation implements ResourceTransformation<V1KafkaTopic> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTopicConfigMapsTransformation.class);

    /**
     * {@inheritDoc
     */
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic toTransform,
                                                     @NotNull HasItems list) {

        var configMapRefs = toTransform.getSpec().getConfigMapRefs();

        if (configMapRefs == null || configMapRefs.isEmpty()) {
            return Optional.of(toTransform);
        }

        ConfigMapList configMapList = ConfigMapList.builder()
                .withItems(list.getAllByClass(ConfigMap.class))
                .build();

        var configsFromConfigMaps = new HashMap<String, Object>();

        for (String configMapRef : configMapRefs) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Executing '{}' on topic '{}' for configMap '{}'",
                        this.getClass(),
                        toTransform.getMetadata().getName(),
                        configMapRef
                );
            }
            ConfigMap configMap = configMapList.findByName(configMapRef)
                    .orElseThrow(() -> new InvalidResourceException(String.format(
                            "Failed to process resource '%s/%s' with 'metadata.name: %s'. Cannot find ConfigMap for '%s'.",
                            toTransform.getApiVersion(),
                            toTransform.getKind(),
                            toTransform.getMetadata().getName(),
                            configMapRef)
                    ));
            Optional.ofNullable(configMap.getData())
                    .ifPresent(config -> configsFromConfigMaps.putAll(config.toMap()));
        }

        var allConfigs = Optional.
                ofNullable(toTransform.getSpec().getConfigs())
                .orElse(Configs.empty());

        allConfigs.addAll(configsFromConfigMaps);

        V1KafkaTopicSpec newSpec = toTransform
                .getSpec()
                .toBuilder()
                .withConfigs(allConfigs)
                .build();

        newSpec = newSpec.withConfigMapRefs(null);

        V1KafkaTopic result = toTransform.toBuilder()
                .withSpec(newSpec)
                .build();

        return Optional.of(result);
    }
}
