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
package io.streamthoughts.jikkou.kafka.command.acls;

import io.streamthoughts.jikkou.kafka.change.ChangeResult;
import io.streamthoughts.jikkou.kafka.command.acls.subcommands.Apply;
import io.streamthoughts.jikkou.kafka.command.acls.subcommands.Create;
import io.streamthoughts.jikkou.kafka.command.acls.subcommands.Delete;
import io.streamthoughts.jikkou.kafka.command.acls.subcommands.Describe;
import io.streamthoughts.jikkou.kafka.config.JikkouConfig;
import io.streamthoughts.jikkou.kafka.manager.KafkaAclsManager;
import io.streamthoughts.jikkou.kafka.manager.KafkaResourceManager;
import io.streamthoughts.jikkou.kafka.manager.KafkaResourceUpdateContext;
import io.streamthoughts.jikkou.kafka.model.V1SpecObject;
import io.streamthoughts.jikkou.kafka.manager.adminclient.AdminClientKafkaAclsManager;
import io.streamthoughts.jikkou.kafka.change.AclChange;
import io.streamthoughts.jikkou.kafka.change.AclChangeOptions;
import io.streamthoughts.jikkou.kafka.command.WithSpecificationCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.List;

@Command(name = "acls",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        header = "Apply the ACLs changes described by your specs-file against the Kafka cluster you are currently pointing at.",
        description = "This command can be used to create ACLs on a remote Kafka cluster",
        subcommands = {
                Apply.class,
                Create.class,
                Delete.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class AclsCommand {

    public static abstract class Base extends WithSpecificationCommand<AclChange> {

        @CommandLine.Option(
                names = "--delete-orphans",
                defaultValue = "false",
                description = "Delete all ACL policies for principals not specified in the specification file"
        )
        Boolean deleteOrphans;

        public abstract KafkaResourceManager.UpdateMode getUpdateMode();

        @Override
        public Collection<ChangeResult<AclChange>> execute(List<V1SpecObject> objects) {

            final KafkaAclsManager manager = new AdminClientKafkaAclsManager(JikkouConfig.get());

            return manager.update(
                    getUpdateMode(),
                    objects,
                    KafkaResourceUpdateContext.with(
                            AclsCommand.Base.this::isResourceCandidate,
                            new AclChangeOptions().withDeleteOrphans(deleteOrphans),
                            isDryRun()
                    )
            );
        }
    }
}
