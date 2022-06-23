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
package io.streamthoughts.jikkou.client.command.quotas;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.client.command.BaseResourceCommand;
import io.streamthoughts.jikkou.client.command.quotas.subcommands.Alter;
import io.streamthoughts.jikkou.client.command.quotas.subcommands.Apply;
import io.streamthoughts.jikkou.client.command.quotas.subcommands.Create;
import io.streamthoughts.jikkou.client.command.quotas.subcommands.Delete;
import io.streamthoughts.jikkou.client.command.quotas.subcommands.Describe;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaList;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(name = "quotas",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Apply the quotas changes described by your specs-file against the Kafka cluster you are currently pointing at.",
        description = "This command can be used to create, alter, delete or describe quotas on a remote Kafka cluster",
        subcommands = {
                Alter.class,
                Apply.class,
                Create.class,
                Delete.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class QuotasCommand {

    public static abstract class Base extends BaseResourceCommand {

        /**
         * {@inheritDoc}
         */
        @Override
        protected @NotNull ResourceList loadResources() {
            ResourceList resourceList = super.loadResources();
            return resourceList.allResourcesForKinds(
                    HasMetadata.getKind(V1KafkaQuotaList.class),
                    HasMetadata.getKind(ConfigMap.class)
            );
        }
    }
}
