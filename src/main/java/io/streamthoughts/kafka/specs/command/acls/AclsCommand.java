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

import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.acl.builder.LiteralAclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.kafka.specs.command.WithAdminClientCommand;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Create;
import io.streamthoughts.kafka.specs.command.acls.subcommands.Describe;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.CreateAclsOperation;
import io.streamthoughts.kafka.specs.resources.AclsResource;
import io.streamthoughts.kafka.specs.resources.Named;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Command(name = "acls",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        header = "Execute changes to the Kafka cluster ACLs.",
        description = "This command can be used to create ACLs on a remote Kafka cluster",
        subcommands = {
                Create.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class AclsCommand extends WithAdminClientCommand {

    public static abstract class Base extends WithSpecificationCommand<AclRule> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<OperationResult<AclRule>> executeCommand(final AdminClient client) {
            final List<OperationResult<AclRule>> results = new LinkedList<>();

            final Optional<AclsResource> optional = clusterSpec().getAcls();

            if (optional.isPresent()) {
                final AclsResource resource = optional.get();
                final AclRulesBuilder builder = AclRulesBuilder.combines(
                        new LiteralAclRulesBuilder(),
                        new TopicMatchingAclRulesBuilder(client));

                final Map<String, AclGroupPolicy> groups = Named.keyByName(resource.getAclGroupPolicies());
                final Collection<AclUserPolicy> users = resource.getAclUsersPolicies();
                List<AclRule> rules = users
                        .stream()
                        .flatMap(user -> builder.toAclRules(groups.values(), user).stream())
                        .collect(Collectors.toList());

                if (isDryRun()) {
                    results.addAll(buildDryRunResults(rules, true, CreateAclsOperation.DESCRIPTION));
                } else {
                    results.addAll(execute(rules, client));
                }
            }
            return results;
        }

        protected abstract Collection<OperationResult<AclRule>> execute(final List<AclRule> rules,
                                                                        final AdminClient client);
    }

    private static List<OperationResult<AclRule>> buildDryRunResults(final Collection<AclRule> resources,
                                                                     final boolean changed,
                                                                     final DescriptionProvider<AclRule> provider) {
        return resources.stream()
                .map(r -> OperationResult.dryRun(r, changed, provider.getForResource(r)))
                .collect(Collectors.toList());
    }
}
