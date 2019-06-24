/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.ClusterSpec;
import io.streamthoughts.kafka.specs.ClusterSpecReader;
import io.streamthoughts.kafka.specs.KafkaSpecsRunnerOptions;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.CreateAclsOperation;
import io.streamthoughts.kafka.specs.operation.ResourceOperationOptions;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExecuteAclCommand implements ClusterCommand<Collection<OperationResult<AclRule>>> {

    private static final Logger LOG = LoggerFactory.getLogger(ExecuteAclCommand.class);

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    private AdminClient client;

    private AclRulesBuilder aclBindingBuilder;

    private KafkaSpecsRunnerOptions options;

    /**
     * Creates a new {@link ExecuteAclCommand} command.
     * @param client    the admin client to be used.
     */
    public ExecuteAclCommand(final AdminClient client,
                             final AclRulesBuilder aclBindingBuilder) {
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(aclBindingBuilder, "aclBindingBuilder cannot be null");
        this.client = client;
        this.aclBindingBuilder = aclBindingBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<AclRule>> execute(final KafkaSpecsRunnerOptions options) {
        Objects.requireNonNull(options, "options cannot be null");
        Objects.requireNonNull(client, "client cannot be null");

        this.options = options;
        File specification = options.clusterSpecificationOpt();
        try {
            // Read input specification
            final ClusterSpec specs = READER.read(new FileInputStream(specification));

            Map<String, AclGroupPolicy> groups = specs.getAclGroupPolicies();
            Collection<AclUserPolicy> users = specs.getAclUsers();

            List<AclRule> rules = users
                    .stream()
                    .flatMap(user -> aclBindingBuilder.toAclRules(groups.values(), user).stream())
                    .collect(Collectors.toList());

            final List<OperationResult<AclRule>> results = new LinkedList<>();
            if (options.isCreateTopicsCommand()) {
                results.addAll(executeCreateAcls(rules));
            }

            return results;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private Collection<OperationResult<AclRule>> executeCreateAcls(final Collection<AclRule> rules) {
        if (options.isDryRun()) {
            return buildDryRunResult(rules, true, CreateAclsOperation.DESCRIPTION);
        }

        return new CreateAclsOperation()
                .execute(client, new ResourcesIterable<>(rules), new ResourceOperationOptions() {});
    }

    private List<OperationResult<AclRule>> buildDryRunResult(final Collection<AclRule> resources,
                                                                   final boolean changed,
                                                                   final DescriptionProvider<AclRule> provider) {
        return resources.stream()
                .map(r -> OperationResult.dryRun(r, changed, provider.getForResource(r)))
                .collect(Collectors.toList());
    }
}
