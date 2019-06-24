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
import io.streamthoughts.kafka.specs.EntityType;
import io.streamthoughts.kafka.specs.KafkaSpecsRunnerOptions;
import io.streamthoughts.kafka.specs.YAMLClusterSpecWriter;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.acl.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.kafka.specs.internal.AdminClientUtils;
import io.streamthoughts.kafka.specs.operation.DescribeAclsOperation;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.DescribeTopicOperation;
import io.streamthoughts.kafka.specs.operation.ResourceOperationOptions;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ExportClusterSpecCommand implements ClusterCommand<Void> {

    private final AdminClient client;

    private final AclRulesBuilder aclRulesBuilder;

    /**
     * Creates a new {@link ExportClusterSpecCommand} instance.
     * @param client    the admin client to be used.
     */
    public ExportClusterSpecCommand(final AdminClient client) {
        this.client = client;
        this.aclRulesBuilder =  new TopicMatchingAclRulesBuilder(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void execute(final KafkaSpecsRunnerOptions options) {
        try {

            Collection<String> topicNames = (options.topics().isEmpty()) ?
                    loadClusterTopicsNames(client) :  options.topics();

            final Collection<TopicResource> resources = new LinkedList<>();
            if (options.entityTypes().contains(EntityType.TOPICS) || options.entityTypes().isEmpty()) {
                List<TopicResource> topics = topicNames.stream().map(TopicResource::new).collect(Collectors.toList());
                ResourcesIterable<TopicResource> it = new ResourcesIterable<>(topics);
                resources.addAll(
                    new DescribeTopicOperation().execute(
                        client,
                        it,
                        DescribeOperationOptions.withDescribeDefaultConfigs(options.isDefaultConfigs())
                    )
                );
            }

            final LinkedList<AclUserPolicy> policies = new LinkedList<>();
            if (options.entityTypes().contains(EntityType.ACLS) || options.entityTypes().isEmpty()) {
                Collection<AclRule> rules = new DescribeAclsOperation().execute(client, null, new ResourceOperationOptions() {});
                policies.addAll(aclRulesBuilder.toAclUserPolicy(rules));
            }

            File file = options.clusterSpecificationOpt();
            OutputStream os = (file != null) ? new FileOutputStream(options.clusterSpecificationOpt()) : System.out;
            YAMLClusterSpecWriter.instance().write(new ClusterSpec(resources, Collections.emptyList(), policies), os);

            return null;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<String> loadClusterTopicsNames(final AdminClient client) {
        CompletableFuture<Collection<TopicListing>> topics = AdminClientUtils.listTopics(client);
        return topics
                .thenApply(t -> t.stream().map(TopicListing::name).collect(Collectors.toList()))
                .join();
    }
}