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

import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.manager.KafkaResourceManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "apply",
        description = "Apply all changes to the Kafka topics."
)
public class Apply extends TopicsCommand.Base {

    @Option(
            names = "--delete-config-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteConfigOrphans;

    @Option(
            names = "--delete-topic-orphans",
            defaultValue = "false",
            description = "Delete Topics which exist on the cluster but absent from the specification files"
    )
    Boolean deleteTopicOrphans;

    @Option(names = "--exclude-internals",
            defaultValue = "true",
            description = "Exclude internal topics (i.e.: __consumer_offset, __transaction_state, connect-[offsets|status|configs], _schemas.)"
    )
    Boolean excludeInternalTopics;

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaResourceManager.UpdateMode getUpdateMode() {
        return KafkaResourceManager.UpdateMode.APPLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicChangeOptions getChangeOptions() {
        return new TopicChangeOptions()
                .withDeleteConfigOrphans(deleteConfigOrphans)
                .withDeleteTopicOrphans(deleteTopicOrphans)
                .withExcludeInternalTopics(excludeInternalTopics);
    }
}
