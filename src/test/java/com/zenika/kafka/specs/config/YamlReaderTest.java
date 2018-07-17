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
package com.zenika.kafka.specs.config;

import com.zenika.kafka.specs.ClusterSpec;
import com.zenika.kafka.specs.YAMLClusterSpecReader;
import com.zenika.kafka.specs.resources.ConfigValue;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.common.config.TopicConfig;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class YamlReaderTest {

    private static final TopicResource TOPIC_P1 = new TopicResource("my-topic-p1", 1, (short)1);

    private static final TopicResource TOPIC_P2 = new TopicResource("my-topic-p2", 2, (short)1)
            .addConfigValue(new ConfigValue(TopicConfig.RETENTION_MS_CONFIG, "10000", false));

    private static final TopicResource TOPIC_P3 = new TopicResource("my-topic-p3", 3, (short)1);


    private static final String THREE_TOPICS_SIMPLE_CONFIG = "topics:\n"+
            "  - name               : my-topic-p3\n"+
            "    partitions         : 3\n"+
            "    replication_factor  : 1\n"+
            "\n"+
            "  - name               : my-topic-p1\n"+
            "    partitions         : 1\n"+
            "    replication_factor  : 1\n"+
            "\n"+
            "  - name               : my-topic-p2\n"+
            "    partitions         : 2\n"+
            "    replication_factor  : 1\n"+
            "    configs: \n" +
            "       retention.ms  : '10000'";


    @Test
    public void shouldReadMultipleTopicResources() throws UnsupportedEncodingException {
        YAMLClusterSpecReader reader = new YAMLClusterSpecReader();
        ClusterSpec description = reader.read(new ByteArrayInputStream(THREE_TOPICS_SIMPLE_CONFIG.getBytes("UTF-8")));
        assertNotNull(description);
        Collection<TopicResource> topics = description.getTopics();
        assertNotNull(topics);
        assertEquals(3, topics.size());
        assertTrue(topics.contains(TOPIC_P1));
        assertTrue(topics.contains(TOPIC_P2));
        assertTrue(topics.contains(TOPIC_P3));
    }

}