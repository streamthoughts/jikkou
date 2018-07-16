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
package com.zenika.kafka.specs;

import com.zenika.kafka.specs.resources.ConfigValue;
import com.zenika.kafka.specs.resources.Configs;
import com.zenika.kafka.specs.resources.TopicResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPICS_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPIC_CONFIGS_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPIC_NAME_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPIC_PARTITIONS_FIELD;
import static com.zenika.kafka.specs.ClusterSpecReader.Fields.TOPIC_REPLICATION_FACTOR_FIELD;

public class YAMLClusterSpecReader implements ClusterSpecReader {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ClusterSpec read(final InputStream stream) {
        Yaml yaml = new Yaml();
        Map<String, Object> descriptionObject = yaml.load(stream);
        if (descriptionObject == null) {
            throw new IllegalArgumentException("Cluster specification is empty or invalid.");
        }
        List<Map<String, Object>> topicObjects = (List<Map<String, Object>>) descriptionObject.get(TOPICS_FIELD);
        Map<String, TopicResource> topics = readTopicsResources(topicObjects);
        return new ClusterSpec(topics);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, TopicResource> readTopicsResources(List<Map<String, Object>> topics) {
        return topics
                .stream()
                .map(YAMLClusterSpecReader::readTopicResource)
                .collect(Collectors.toMap(TopicResource::name, o -> o));
    }

    private static TopicResource readTopicResource(final Map<String, Object> m) {
        final Integer partitions = getOrFailed(TOPIC_PARTITIONS_FIELD, m);
        final short replication = ((Integer) getOrFailed(TOPIC_REPLICATION_FACTOR_FIELD, m)).shortValue();
        final Map<String, String> configs = getOrElse(TOPIC_CONFIGS_FIELD, m, Collections.emptyMap());

        Set<ConfigValue> configValues = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        return new TopicResource(getOrFailed(TOPIC_NAME_FIELD, m), partitions, replication, new Configs(configValues));
    }

    @SuppressWarnings("unchecked")
    private static <T>  T getOrElse(String key, Map<String, Object> objectMap, T defaultVal) {
        if (!objectMap.containsKey(key)) return defaultVal;
        return (T) objectMap.get(key);
    }

    @SuppressWarnings("unchecked")
    private static <T>  T getOrFailed(final String key, final Map<String, Object> objectMap) {
        if (!objectMap.containsKey(key)) throw new RuntimeException("missing key '" + key + "'");
        return (T) objectMap.get(key);
    }
}
