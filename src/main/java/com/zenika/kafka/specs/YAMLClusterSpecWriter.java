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

import com.zenika.kafka.specs.resources.Configs;
import com.zenika.kafka.specs.resources.TopicResource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default interface to write a cluster specification.
 */
public class YAMLClusterSpecWriter implements ClusterSpecWriter {

    private static YAMLClusterSpecWriter INSTANCE = new YAMLClusterSpecWriter();

    public static YAMLClusterSpecWriter instance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final ClusterSpec spec, final OutputStream os) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        yaml.setBeanAccess(BeanAccess.DEFAULT);
        yaml.dump(toWrittableMap(spec), new OutputStreamWriter(os) );
    }

    private Map<String, Object> toWrittableMap(final ClusterSpec spec) {
        List<Map<String, Object>> outputTopics = new LinkedList<>();
        for (TopicResource resource : spec.getTopics()) {
            Map<String, Object> outputTopicMap = new LinkedHashMap<>();
            outputTopicMap.put(ClusterSpecReader.Fields.TOPIC_NAME_FIELD, resource.name());
            outputTopicMap.put(ClusterSpecReader.Fields.TOPIC_PARTITIONS_FIELD, resource.partitions());
            outputTopicMap.put(ClusterSpecReader.Fields.TOPIC_REPLICATION_FACTOR_FIELD, resource.replicationFactor());
            outputTopicMap.put(ClusterSpecReader.Fields.TOPIC_CONFIGS_FIELD, Configs.asStringValueMap(resource.configs()));
            outputTopics.add(outputTopicMap);
        }
        Map<String, Object> outputClusterMap = new HashMap<>();
        outputClusterMap.put(ClusterSpecReader.Fields.TOPICS_FIELD, outputTopics);

        return outputClusterMap;
    }
}
