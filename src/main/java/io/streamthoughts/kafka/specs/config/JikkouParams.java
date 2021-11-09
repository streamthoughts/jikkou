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
package io.streamthoughts.kafka.specs.config;

import io.streamthoughts.kafka.specs.internal.PropertiesUtils;
import io.streamthoughts.kafka.specs.transforms.Transformation;
import io.streamthoughts.kafka.specs.validations.Validation;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
            .ofInt("validation.topic.min.replication.factor").orElse(1);

    public static final ConfigParam<Integer> VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG = ConfigParam
            .ofInt("validation.topic.min.num.partitions").orElse(1);

    public static final ConfigParam<String> VALIDATION_TOPIC_NAME_REGEX_CONFIG = ConfigParam
            .ofString("validation.topic.name.regex");

    public static final ConfigParam<List<Class<Validation>>> VALIDATIONS_CONFIG = ConfigParam
            .ofClasses("validations");

    public static final ConfigParam<List<Class<Transformation>>> TRANSFORMATIONS_CONFIG = ConfigParam
            .ofClasses("transforms");

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
