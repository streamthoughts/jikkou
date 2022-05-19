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

import com.typesafe.config.Config;
import io.streamthoughts.jikkou.kafka.internal.ClassUtils;
import io.streamthoughts.jikkou.kafka.manager.KafkaAclsManager;
import io.streamthoughts.jikkou.kafka.manager.KafkaBrokerManager;
import io.streamthoughts.jikkou.kafka.manager.KafkaQuotaManager;
import io.streamthoughts.jikkou.kafka.manager.KafkaTopicManager;
import io.vavr.Tuple2;
import io.vavr.control.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The list of configuration parameters.
 */
public final class JikkouParams {

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

    public static final ConfigParam<KafkaBrokerManager> KAFKA_BROKERS_MANAGER = getConfigurableInstance("managers.kafka.brokers");

    public static final ConfigParam<KafkaTopicManager> KAFKA_TOPICS_MANAGER = getConfigurableInstance("managers.kafka.topics");

    public static final ConfigParam<KafkaAclsManager> KAFKA_ACLS_MANAGER = getConfigurableInstance("managers.kafka.acls");

    public static final ConfigParam<KafkaQuotaManager> KAFKA_QUOTAS_MANAGER = getConfigurableInstance("managers.kafka.quotas");

    private static <T extends Configurable> ConfigParam<T> getConfigurableInstance(final String key) {
        return new ConfigParam<>(
                key,
                (path, rootConfig) -> {
                    if (!rootConfig.unwrap().hasPath(path)) return Option.none();
                    Config innerConfig = rootConfig.unwrap().getConfig(path);

                    JikkouConfig instanceConfig = new JikkouConfig(innerConfig, false);
                    Class<T> managerClass = instanceConfig.getClass("type");
                    Option<JikkouConfig> managerConfig = instanceConfig.findConfig("config");

                    T manager = ClassUtils.newInstance(managerClass);
                    manager.configure(managerConfig.getOrElse(JikkouConfig.empty()).withFallback(rootConfig));
                    return Option.of(manager);
                }
        );
    }
}
