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
import com.zenika.kafka.specs.operation.DescribeOperationOptions;
import com.zenika.kafka.specs.operation.DescribeTopicOperation;
import com.zenika.kafka.specs.resources.Named;
import com.zenika.kafka.specs.resources.ResourcesIterable;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ExportTopicsCommand implements ClusterCommand<Void> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Void execute(final KafkaSpecsRunnerOptions options,
                        final AdminClient client) {
        try {
            Collection<String> topicNames = (options.topics().isEmpty()) ?
                    client.listTopics().names().get() :
                    options.topics();
            List<TopicResource> topics = topicNames.stream().map(TopicResource::new).collect(Collectors.toList());
            ResourcesIterable<TopicResource> it = new ResourcesIterable<>(topics);

            Collection<TopicResource> resources = new DescribeTopicOperation().execute(client, it,
                    DescribeOperationOptions.withDescribeDefaultConfigs(options.isDefaultConfigs()));

            File file = options.clusterSpecificationOpt();
            OutputStream os = (file != null) ? new FileOutputStream(options.clusterSpecificationOpt()) : System.out;
            YAMLClusterSpecWriter.instance().write(new ClusterSpec(Named.keyByName(resources)), os);

            return null;

        } catch (InterruptedException | ExecutionException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}