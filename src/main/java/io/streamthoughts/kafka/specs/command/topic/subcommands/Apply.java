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
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.topics.AlterTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.CreateTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.DeleteTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;
import io.vavr.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "apply",
         description = "Apply all changes to the Kafka topics."
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
    public TopicChangeOptions getOptions() {
        return new TopicChangeOptions()
                .withDeleteConfigOrphans(deleteConfigOrphans)
                .withDeleteTopicOrphans(deleteTopicOrphans)
                .withExcludeInternalTopics(excludeInternalTopics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicOperation getOperation(@NotNull final AdminClient client) {
        return new TopicOperation() {
            final CreateTopicOperation create = new CreateTopicOperation(client);
            final AlterTopicOperation alter = new AlterTopicOperation(client);
            final DeleteTopicOperation delete = new DeleteTopicOperation(client);

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

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean test(final TopicChange change) {
                return delete.test(change) || create.test(change) || alter.test(change);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public @NotNull Map<String, List<Future<Void>>> doApply(final @NotNull Collection<TopicChange> changes) {
                HashMap<String, List<Future<Void>>> results = new HashMap<>();
                results.putAll(delete.apply(changes));
                results.putAll(create.apply(changes));
                results.putAll(alter.apply(changes));
                return results;
            }
        };
    }
}
