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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import io.streamthoughts.kafka.specs.internal.ConfigsBuilder;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default command to alter multiple topics.
 */
public class AlterTopicOperation extends TopicOperation<ResourceOperationOptions> {

    public static DescriptionProvider<TopicResource> DESCRIPTION = (resource -> {
        return (Description.Alter) () -> String.format("Alter topic %s", resource.name());
    });

    private static final Logger LOG = LoggerFactory.getLogger(AlterTopicOperation.class);

    /**
     * {@inheritDoc}
     */
    @Override
    Description getDescriptionFor(final TopicResource resource) {
        return DESCRIPTION.getForResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, KafkaFuture<Void>> doExecute(final AdminClient client,
                                                       final ResourcesIterable<TopicResource> resources,
                                                       final ResourceOperationOptions options) {
        final ConfigsBuilder builder = new ConfigsBuilder();

        final List<String> topicsNames = new ArrayList<>();
        resources.forEach(resource -> {
            final ConfigsBuilder.ResourceConfigSupplier topicConfigs =
                    builder.newResourceConfig()
                            .setType(ConfigResource.Type.TOPIC)
                            .setName(resource.name());
            topicsNames.add(resource.name());

            resource.configs().forEach(v -> topicConfigs.setConfig(v.name(), v.getValue()));
        });
        LOG.info("Starting to alter topics {}", topicsNames);
        AlterConfigsResult result = client.alterConfigs(builder.build());

        final Map<String, KafkaFuture<Void>> futures = new HashMap<>();
        result.values().forEach( (k, v) -> futures.put(k.name(), v));
        return futures;
    }
}
