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
package io.streamthoughts.kafka.specs.reader;

import io.streamthoughts.kafka.specs.resources.BrokerResource;
import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.TopicResource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A reader for {@link TopicResource}.
 */
public class BrokerClusterSpecReader implements EntitySpecificationReader<BrokerResource> {

    public static final String BROKER_HOST_FIELD    = "host";
    public static final String BROKER_PORT_FIELD    = "port";
    public static final String BROKER_RACK_FIELD    = "rack";
    public static final String BROKER_ID_FIELD      = "id";
    public static final String BROKER_CONFIGS_FIELD = "configs";

    /**
     * @return a new {@link TopicResource} instance.
     */
    @Override
    public BrokerResource to(final MapObjectReader m) {
        final Map<String, String> configs = m.getOrElse(BROKER_CONFIGS_FIELD, Collections.emptyMap());

        Set<ConfigValue> configValues = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        return new BrokerResource(
                m.get(BROKER_ID_FIELD),
                m.get(BROKER_HOST_FIELD),
                m.get(BROKER_PORT_FIELD),
                m.get(BROKER_RACK_FIELD),
                new Configs(configValues)
        );
    }
}
