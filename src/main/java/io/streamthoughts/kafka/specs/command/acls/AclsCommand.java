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

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.change.AclChange;
import io.streamthoughts.kafka.specs.change.AclChanges;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Apply;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Delete;
import io.streamthoughts.kafka.specs.command.acls.subcommands.internal.DescribeACLs;
import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.resources.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.model.V1AccessUserObject;
import io.streamthoughts.kafka.specs.resources.acl.builder.LiteralAclRulesBuilder;
import io.streamthoughts.kafka.specs.resources.acl.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.kafka.specs.command.WithAdminClientCommand;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Create;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Describe;
import io.streamthoughts.kafka.specs.model.V1SecurityObject;
import io.streamthoughts.kafka.specs.operation.acls.AclOperation;
import io.streamthoughts.kafka.specs.resources.Named;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class AclsCommand extends WithAdminClientCommand {

    public static abstract class Base extends WithSpecificationCommand<AclChange> {

        @CommandLine.Option(
                names = "--delete-orphans",
                defaultValue = "false",
                description = "Delete all ACL policies for principals not specified in the specification file"
        )
        Boolean deleteOrphans;

        /**
         * Gets the operation to execute.
         *
         * @param client    the {@link AdminClient}.
         * @return          a new {@link AclOperation}.
         */
        public abstract AclOperation getOperation(@NotNull final AdminClient client);

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<OperationResult<AclChange>> executeCommand(final AdminClient client) {
            final Optional<V1SecurityObject> optional = loadSpecsObject().security();

            if (optional.isEmpty()) {
                return Collections.emptyList();
            }

            final V1SecurityObject resource = optional.get();
            final AclRulesBuilder builder = AclRulesBuilder.combines(
                    new LiteralAclRulesBuilder(),
                    new TopicMatchingAclRulesBuilder(client));

            final Map<String, V1AccessRoleObject> groups = Named.keyByName(resource.roles());
            final Collection<V1AccessUserObject> users = resource.users();

            List<AccessControlPolicy> newPolicies = users
                    .stream()
                    .flatMap(user -> builder.toAccessControlPolicy(groups.values(), user).stream())
                    .filter(this::isResourceCandidate)
                    .collect(Collectors.toList());

            List<AccessControlPolicy> oldPolicies = new DescribeACLs(client)
                    .describe()
                    .stream()
                    .filter(this::isResourceCandidate)
                    .collect(Collectors.toList());

            final AclOperation operation = getOperation(client);

            final AclChanges changes = AclChanges.computeChanges(oldPolicies, newPolicies, deleteOrphans);

            final LinkedList<OperationResult<AclChange>> results = new LinkedList<>();

            if (isDryRun()) {
                changes.all()
                        .stream()
                        .filter(it -> operation.test(it) || it.getOperation() == Change.OperationType.NONE)
                        .map(change -> {
                            Description description = operation.getDescriptionFor(change);
                            return change.getOperation() == Change.OperationType.NONE ?
                                    OperationResult.ok(change, description) :
                                    OperationResult.changed(change, description);
                        })
                        .forEach(results::add);
            } else {
                results.addAll(changes.apply(operation));
                changes.all()
                        .stream()
                        .filter(it -> it.getOperation() == Change.OperationType.NONE)
                        .map(change -> OperationResult.ok(change, operation.getDescriptionFor(change)))
                        .forEach(results::add);
            }

            return results;
        }
    }
}
