/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.internal;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static io.streamthoughts.kafka.specs.internal.FutureUtils.toCompletableFuture;

/**
 * Class which is used to create a new {@link AdminClient} instance using tool arguments.
 */
public class KafkaUtils {

    public static AdminClient newAdminClient(@NotNull final Properties clientConfigProps){
        return AdminClient.create(clientConfigProps);
    }

    public static CompletableFuture<Collection<Node>> listBrokers(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        return toCompletableFuture(client.describeCluster().nodes());
    }

    public static CompletableFuture<Collection<TopicListing>> listTopics(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        return toCompletableFuture(client.listTopics().listings());
    }

    public static boolean waitForKafkaBrokers(final AdminClient client, final KafkaBrokersReady.Options options) {
        return new KafkaBrokersReady(options).waitForBrokers(client);
    }
}
