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
package io.streamthoughts.jikkou.client.command.topic.subcommands;

import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.client.command.topic.TopicsCommand;
import io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "delete",
        description = "Delete all topics not described in the specification file."
)
public class Delete extends TopicsCommand.Base {

    @Option(names = "--exclude-internals",
            description = "Exclude internal topics (i.e.: __consumer_offset, __transaction_state, connect-[offsets|status|configs], _schemas.)"
    )
    boolean excludeInternalTopics = true;

    /** {@inheritDoc} */
    @Override
    public ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.DELETE_ONLY;
    }

    /** {@inheritDoc} */
    @Override
    public Configuration getReconciliationConfiguration() {
        return new KafkaTopicReconciliationConfig()
                .withExcludeInternalTopics(excludeInternalTopics)
                .withDeleteTopicOrphans(true)
                .asConfiguration();
    }
}
