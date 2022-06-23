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
package io.streamthoughts.jikkou.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.api.io.ResourceTypeResolver;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.kafka.models.V1KafkaCluster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LegacyKafkaClusterResourceTypeResolver implements ResourceTypeResolver {

    /** {@inheritDoc} */
    @Override
    public @Nullable Class<? extends Resource> resolvesType(@NotNull JsonNode node) {
        JsonNode apiVersion = node.get("apiVersion");
        if (apiVersion == null || apiVersion.asText().equalsIgnoreCase("1")) {
            return V1KafkaCluster.class;
        }
        return null;
    }
}
