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
package com.zenika.kafka.specs.command;

import com.zenika.kafka.specs.ClusterSpec;
import com.zenika.kafka.specs.KafkaSpecsRunnerOptions;
import com.zenika.kafka.specs.YAMLClusterSpecWriter;
import com.zenika.kafka.specs.acl.AclRule;
import com.zenika.kafka.specs.acl.AclRulesBuilder;
import com.zenika.kafka.specs.acl.AclUserPolicy;
import com.zenika.kafka.specs.acl.builder.TopicMatchingAclRulesBuilder;
import com.zenika.kafka.specs.internal.AdminClientUtils;
import com.zenika.kafka.specs.operation.DescribeAclsOperation;
import com.zenika.kafka.specs.operation.DescribeOperationOptions;
import com.zenika.kafka.specs.operation.DescribeTopicOperation;
import com.zenika.kafka.specs.operation.ResourceOperationOptions;
import com.zenika.kafka.specs.resources.ResourcesIterable;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
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

            List<TopicResource> topics = topicNames.stream().map(TopicResource::new).collect(Collectors.toList());
            ResourcesIterable<TopicResource> it = new ResourcesIterable<>(topics);

            Collection<TopicResource> resources = new DescribeTopicOperation().execute(client, it,
                    DescribeOperationOptions.withDescribeDefaultConfigs(options.isDefaultConfigs()));

            Collection<AclRule> rules = new DescribeAclsOperation().execute(client, null, new ResourceOperationOptions() {});
            Collection<AclUserPolicy> policies = aclRulesBuilder.toAclUserPolicy(rules);


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