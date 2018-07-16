/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs.operation;

import com.zenika.kafka.specs.command.TopicsCommands;
import com.zenika.kafka.specs.resources.Configs;
import com.zenika.kafka.specs.resources.ResourcesIterable;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default command to create multiple topics.
 */
public class CreateTopicOperation extends TopicOperation<CreateTopicOperationOptions> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicOperation.class);

    /**
     * {@inheritDoc}
     */
    @Override
    TopicsCommands getCommand() {
        return TopicsCommands.CREATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, KafkaFuture<Void>> doExecute(final AdminClient client,
                                                       final ResourcesIterable<TopicResource> resources,
                                                       final ResourceOperationOptions options) {
        List<NewTopic> topics = StreamSupport.stream(resources.spliterator(), false)
                .map(this::toNewTopic)
                .collect(Collectors.toList());
        LOG.info("Creating new topics : {}", topics);
        CreateTopicsResult result = client.createTopics(topics, new CreateTopicsOptions());

        return result.values();
    }

    private NewTopic toNewTopic(final TopicResource t) {
        Map<String, String> resourceConfig = Configs.asStringValueMap(t.configs());
        return new NewTopic(t.name(), t.partitions(), t.replicationFactor())
                .configs(resourceConfig);
    }

}
