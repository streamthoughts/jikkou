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
package io.streamthoughts.kafka.specs.command.topic.subcommands;

import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.TopicCandidates;
import io.streamthoughts.kafka.specs.operation.OperationType;
import io.streamthoughts.kafka.specs.operation.CreateTopicOperation;
import io.streamthoughts.kafka.specs.operation.CreateTopicOperationOptions;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine.Command;

import java.util.Collection;

@Command(name = "create",
         description = "Create the topics missing on the cluster as describe in the specification file."
)
public class Create extends TopicsCommand.Base {
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<TopicResource>> execute(final Collection<TopicResource> topics,
                                                              final AdminClient client) {
        return new CreateTopicOperation()
                .execute(
                        client,
                        new ResourcesIterable<>(topics),
                        new CreateTopicOperationOptions()
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TopicResource> getTopics(final TopicCandidates candidates) {
        return candidates.topicsToCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationType getOperationType() {
        return OperationType.CREATE;
    }
}
