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
package io.streamthoughts.jikkou.kafka.config;

import io.streamthoughts.jikkou.kafka.internal.PropertiesUtils;
import io.vavr.Tuple2;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The list of configuration parameters.
 */
public final class JikkouParams {

    public static final String ADMIN_CLIENT_CONFIG_NAME = "adminClient";

    public static final ConfigParam<Properties> ADMIN_CLIENT_CONFIG = ConfigParam
            .ofMap(ADMIN_CLIENT_CONFIG_NAME)
            .orElse(HashMap::new)
            .map(JikkouParams::getAdminClientConfigs)
            .map(PropertiesUtils::fromMap);

    public static final ConfigParam<Map<String, Object>> TEMPLATING_VARS_CONFIG = ConfigParam
            .ofMap("templating.vars").orElse(HashMap::new);

    public static final ConfigParam<Integer> VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG = ConfigParam
            .ofInt("topic-min-replication-factor");

    public static final ConfigParam<Integer> VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG = ConfigParam
            .ofInt("topic-min-num-partitions");

    public static final ConfigParam<String> VALIDATION_TOPIC_NAME_REGEX_CONFIG = ConfigParam
            .ofString("topic-name-regex");

    public static final ConfigParam<List<String>> VALIDATION_TOPIC_NAME_PREFIXES_CONFIG = ConfigParam
            .ofList("topic-name-prefixes-allowed");

    public static final ConfigParam<List<String>> VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG = ConfigParam
            .ofList("topic-name-suffixes-allowed");

    public static final ConfigParam<List<Tuple2<String, JikkouConfig>>> VALIDATIONS_CONFIG = ConfigParam
            .ofConfigs("validations")
            .map(configs -> configs.stream()
                       .map(o -> new JikkouConfig(o, false))
                       .map(config ->  new Tuple2<>(
                               config.getString("type"),
                               config.findConfig("config").getOrElse(JikkouConfig.empty())
                       ))
                       .collect(Collectors.toList())
            );

    public static final ConfigParam<List<Tuple2<String, JikkouConfig>>> TRANSFORMATIONS_CONFIG = ConfigParam
            .ofConfigs("transforms")
            .map(configs -> configs.stream()
                    .map(o -> new JikkouConfig(o, false))
                    .map(config ->  new Tuple2<>(
                            config.getString("type"),
                            config.findConfig("config").getOrElse(JikkouConfig.empty())
                    ))
                    .collect(Collectors.toList())
            );
    public static final ConfigParam<List<String>> EXTENSION_PATHS = ConfigParam
            .ofList("extension.paths");

    public static final ConfigParam<Pattern[]> INCLUDE_RESOURCES = ConfigParam
            .ofList("include-resources")
            .map(l -> l.stream().map(Pattern::compile).collect(Collectors.toList()))
            .orElse(Collections::emptyList)
            .map(l -> l.toArray(Pattern[]::new));

    public static final ConfigParam<Pattern[]> EXCLUDE_RESOURCES = ConfigParam
            .ofList("exclude-resources")
            .map(l -> l.stream().map(Pattern::compile).collect(Collectors.toList()))
            .orElse(Collections::emptyList)
            .map(l -> l.toArray(Pattern[]::new));

    public static final ConfigParam<Boolean> KAFKA_BROKERS_WAIT_FOR_ENABLED = ConfigParam
            .ofBoolean("kafka.brokers.wait-for-enabled");

    public static final ConfigParam<Integer> KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE = ConfigParam
            .ofInt("kafka.brokers.wait-for-min-available");

    public static final ConfigParam<Long> KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS = ConfigParam
            .ofLong("kafka.brokers.wait-for-retry-backoff-ms");

    public static final ConfigParam<Long> KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS = ConfigParam
            .ofLong("kafka.brokers.wait-for-timeout-ms");

    private static Map<String, Object> getAdminClientConfigs(final Map<String, Object> configs) {
        return getConfigsForKeys(configs, AdminClientConfig.configNames());
    }

    private static Map<String, Object> getConfigsForKeys(final Map<String, Object> configs,
                                                         final Set<String> keys) {
        final Map<String, Object> parsed = new HashMap<>();
        for (final String configName : keys) {
            if (configs.containsKey(configName)) {
                parsed.put(configName, configs.get(configName));
            }
        }
        return parsed;
    }
}
