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
package io.streamthoughts.kafka.specs.internal;

import io.streamthoughts.kafka.specs.KafkaSpecsRunnerOptions;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaFuture;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.streamthoughts.kafka.specs.internal.PropertiesUtils.parse;

/**
 * Class which is used to create a new {@link AdminClient} instance using tool arguments.
 */
public class AdminClientUtils {

    public static AdminClient newAdminClient(final KafkaSpecsRunnerOptions opts){
        Properties props = getClientConfig(opts);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, opts.bootstrapServerOpt());
        return AdminClient.create(props);
    }

    private static Properties getClientConfig(final KafkaSpecsRunnerOptions opts) {
        final Properties props = new Properties();
        if (opts.configPropsFileOpt() != null) {
            File configFile = opts.configPropsFileOpt();
            if (!configFile.exists() || !configFile.canRead()) {
                throw new IllegalArgumentException("Invalid argument : File doesn't exist or is not readable : ' " + configFile.getPath() + " ' ");
            }
            try {
                Properties properties = PropertiesUtils.loadProps(configFile.getPath());
                props.putAll(properties);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid argument : File doesn't exist or is not readable : ' " + configFile.getPath() + " ' ");
            }
        }
        if (!opts.configPropsOpts().isEmpty()) {
            props.putAll(parse(opts.configPropsOpts()));
        }
        return props;
    }


    public static CompletableFuture<Collection<TopicListing>> listTopics(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");

        KafkaFuture<Collection<TopicListing>> listings = client.listTopics().listings();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return listings.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
