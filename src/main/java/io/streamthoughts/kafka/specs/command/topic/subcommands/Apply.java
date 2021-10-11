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

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChanges;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.streamthoughts.kafka.specs.change.Change.OperationType.ADD;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.DELETE;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.UPDATE;

@Command(name = "apply",
         description = "Apply changes to the Kafka topics."
)
public class Apply extends TopicsCommand.Base {

    @CommandLine.Option(
            names = "--delete-config-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteConfigOrphans;

    @CommandLine.Option(
            names = "--delete-topic-orphans",
            defaultValue = "false",
            description = "Delete Topics which exist on the cluster but absent from the specification files"
    )
    Boolean deleteTopicOrphans;

    @CommandLine.Option(names = "--exclude-internals",
            description = "Exclude internal topics (i.e.: __consumer_offset, __transaction_state, connect-[offsets|status|configs], _schemas.)"
    )
    boolean excludeInternalTopics = true;

    public static DescriptionProvider<TopicChange> DESCRIPTION = resource ->
            (Description.None) () -> String.format("Unchanged topic %s ", resource.name());

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicOperation getOperation(@NotNull final AdminClient client) {
        return new TopicOperation() {
            final CreateTopicOperation create = new CreateTopicOperation(client, new CreateTopicOperationOptions());
            final AlterTopicOperation alter = new AlterTopicOperation(client, deleteConfigOrphans);
            final DeleteTopicOperation delete = new DeleteTopicOperation(client, excludeInternalTopics);

            @Override
            public Description getDescriptionFor(final @NotNull TopicChange change) {
                switch (change.getOperation()) {
                    case ADD:
                        return create.getDescriptionFor(change);
                    case UPDATE:
                        return alter.getDescriptionFor(change);
                    case DELETE:
                        return delete.getDescriptionFor(change);
                    case NONE:
                        return DESCRIPTION.getForResource(change);
                    default:
                        throw new UnsupportedOperationException("Unsupported operation type: " + change.getOperation());
                }
            }

            @Override
            public boolean test(final TopicChange change) {
                Change.OperationType operation = change.getOperation();
                return (operation == DELETE && deleteTopicOrphans) || List.of(ADD, UPDATE).contains(operation);
            }

            @Override
            public Map<String, KafkaFuture<Void>> apply(final @NotNull TopicChanges changes) {
                 HashMap<String, KafkaFuture<Void>> results = new HashMap<>();
                 if (deleteTopicOrphans) {
                     results.putAll(delete.apply(changes));
                 }
                results.putAll(create.apply(changes));
                results.putAll(alter.apply(changes));
                return results;
            }
        };
    }
}
