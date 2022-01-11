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
package io.streamthoughts.kafka.specs.command.acls;

import io.streamthoughts.kafka.specs.change.AclChange;
import io.streamthoughts.kafka.specs.change.AclChangeOptions;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Apply;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Create;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Delete;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Describe;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.manager.KafkaAclsManager;
import io.streamthoughts.kafka.specs.manager.KafkaResourceManager;
import io.streamthoughts.kafka.specs.manager.KafkaResourceOperationContext;
import io.streamthoughts.kafka.specs.manager.adminclient.AdminClientKafkaAclsManager;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

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
        public Collection<ChangeResult<AclChange>> execute(List<V1SpecsObject> objects) {

            final KafkaAclsManager manager = new AdminClientKafkaAclsManager();
            manager.configure(JikkouConfig.get());

            return manager.update(
                    getUpdateMode(),
                    objects,
                    new KafkaResourceOperationContext<>() {
                        @Override
                        public Predicate<String> getResourcePredicate() {
                            return AclsCommand.Base.this::isResourceCandidate;
                        }

                        @Override
                        public AclChangeOptions getOptions() {
                            return new AclChangeOptions().withDeleteOrphans(deleteOrphans);
                        }

                        @Override
                        public boolean isDryRun() {
                            return AclsCommand.Base.this.isDryRun();
                        }
                    }
            );
        }
    }
}
