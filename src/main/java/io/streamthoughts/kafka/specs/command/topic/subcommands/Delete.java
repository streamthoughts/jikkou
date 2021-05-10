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
import io.streamthoughts.kafka.specs.operation.DeleteTopicOperation;
import io.streamthoughts.kafka.specs.operation.OperationType;
import io.streamthoughts.kafka.specs.operation.ResourceOperationOptions;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Command(name = "delete",
        description = "Delete all topics not described in the specification file."
)
public class Delete extends TopicsCommand.Base {

    private static final Set<String> INTERNAL_TOPICS = Set.of(
            "__consumer_offsets",
            "_schemas",
            "__transaction_state",
            "connect-offsets",
            "connect-status",
            "connect-configs"
    );

    @Option(names = "--exclude-internals",
            description = "Exclude internal topics (i.e.: __consumer_offset, __transaction_state, connect-[offsets|status|configs], _schemas.)"
    )
    boolean excludeInternalTopics = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<TopicResource>> execute(final Collection<TopicResource> topics,
                                                              final AdminClient client) {

        final Collection<TopicResource> filtered;
        if (excludeInternalTopics) {
            filtered = topics
                .stream()
                .filter(this::isNotInternalTopics)
                .collect(Collectors.toList());
        } else {
            filtered = topics;
        }

        return new DeleteTopicOperation()
                .execute(
                        client,
                        new ResourcesIterable<>(filtered),
                        new ResourceOperationOptions() {
                        }
                );
    }

    private boolean isNotInternalTopics(final TopicResource topic) {
        return !INTERNAL_TOPICS.contains(topic.name()) && !topic.name().startsWith("__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TopicResource> getTopics(final TopicCandidates candidates) {
        return candidates.topicsExistingOnlyOnCluster();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationType getOperationType() {
        return OperationType.DELETE;
    }
}
