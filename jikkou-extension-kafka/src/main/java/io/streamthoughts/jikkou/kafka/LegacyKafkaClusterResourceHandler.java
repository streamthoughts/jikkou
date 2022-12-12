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

import io.streamthoughts.jikkou.api.ResourceListHandler;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaCluster;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClusterSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class LegacyKafkaClusterResourceHandler implements ResourceListHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ResourceList handle(@NotNull ResourceList resources) {
        return new ResourceList(resources
                .stream()
                .flatMap(resource -> {
                    return V1KafkaCluster.class.isAssignableFrom(resource.getClass()) ?
                            adapt((V1KafkaCluster) resource) : Stream.of(resource);
                }).toList());
    }

    private static Stream<HasMetadata> adapt(final V1KafkaCluster cluster) {
        List<HasMetadata> resources = new LinkedList<>();
        V1KafkaClusterSpec spec = cluster.getSpec();
        List<V1KafkaTopicObject> topics = spec.getTopics();
        if (topics != null && topics.size() > 0) {
            V1KafkaTopicList kafkaTopicList = new V1KafkaTopicList().toBuilder()
                    .withSpec(V1KafkaTopicSpec.builder()
                            .withTopics(topics)
                            .build()
                    ).build();
            resources.add(kafkaTopicList);
        }

        List<V1KafkaQuotaObject> quotas = spec.getQuotas();
        if (quotas != null && quotas.size() > 0) {
            V1KafkaQuotaList kafkaQuotaList = new V1KafkaQuotaList().toBuilder()
                    .withSpec(V1KafkaQuotaSpec
                            .builder()
                            .withQuotas(quotas)
                            .build()
                    )
                    .build();
            resources.add(kafkaQuotaList);
        }

        V1KafkaAuthorizationSpec security = spec.getSecurity();
        if (security != null && (security.getRoles().size() > 0 || security.getUsers().size() > 0 )) {
            var authorizationSpecBuilder = V1KafkaAuthorizationSpec.builder();
            authorizationSpecBuilder
                    .withRoles(security.getRoles())
                    .withUsers(security.getUsers());
            var authorizationList = new V1KafkaAuthorizationList()
                    .toBuilder()
                    .withSpec(authorizationSpecBuilder.build())
                    .build();
            resources.add(authorizationList);

        }
        return resources.stream();
    }
}
