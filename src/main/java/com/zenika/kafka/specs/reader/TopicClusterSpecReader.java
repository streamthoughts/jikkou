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
package com.zenika.kafka.specs.reader;

import com.zenika.kafka.specs.resources.ConfigValue;
import com.zenika.kafka.specs.resources.Configs;
import com.zenika.kafka.specs.resources.TopicResource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A reader for {@link TopicResource}.
 */
public class TopicClusterSpecReader implements EntitySpecificationReader<TopicResource> {

    public static final String TOPIC_PARTITIONS_FIELD          = "partitions";
    public static final String TOPIC_REPLICATION_FACTOR_FIELD  = "replication_factor";
    public static final String TOPIC_NAME_FIELD                = "name";
    public static final String TOPIC_CONFIGS_FIELD             = "configs";

    /**
     * @return a new {@link TopicResource} instance.
     */
    @Override
    public TopicResource to(final MapObjectReader m) {
        final Integer partitions = m.get(TOPIC_PARTITIONS_FIELD);
        final short replication = ((Integer) m.get(TOPIC_REPLICATION_FACTOR_FIELD)).shortValue();
        final Map<String, String> configs = m.getOrElse(TOPIC_CONFIGS_FIELD, Collections.emptyMap());

        Set<ConfigValue> configValues = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        return new TopicResource(m.get(TOPIC_NAME_FIELD), partitions, replication, new Configs(configValues));
    }
}
