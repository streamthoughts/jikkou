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
package com.zenika.kafka.specs.operation;

import com.zenika.kafka.specs.OperationResult;
import com.zenika.kafka.specs.resources.Named;
import com.zenika.kafka.specs.resources.ResourcesIterable;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class TopicOperation<T extends ResourceOperationOptions> extends AbstractOperation<TopicResource, T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<TopicResource>> execute(final AdminClient client,
                                                              final ResourcesIterable<TopicResource> resources,
                                                              final T options) {

        final Map<String, KafkaFuture<Void>> values = doExecute(client, resources, options);

        final Map<String, TopicResource> topicKeyedByName = Named.keyByName(resources.originalCollections());
        List<CompletableFuture<OperationResult<TopicResource>>> completableFutures = values.entrySet()
                .stream()
                .map(entry -> {
                    final KafkaFuture<Void> future = entry.getValue();
                    return makeCompletableFuture(future, topicKeyedByName.get(entry.getKey()));
                }).collect(Collectors.toList());

        return completableFutures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

    }

    protected abstract Map<String, KafkaFuture<Void>> doExecute(final AdminClient client,
                                                                final ResourcesIterable<TopicResource> resource,
                                                                final ResourceOperationOptions options);

}
