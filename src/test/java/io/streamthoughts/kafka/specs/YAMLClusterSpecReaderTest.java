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
package io.streamthoughts.kafka.specs;

import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.common.config.TopicConfig;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class YAMLClusterSpecReaderTest {

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

    private static final String ACL_GROUP_POLICIES =
            "acls:\n" +
                    "  group_policies:\n" +
                    "    - name : 'group_one'\n" +
                    "      resource :\n" +
                    "        type : 'topic'\n" +
                    "        pattern : '/([.-])*/'\n" +
                    "        patternType : 'MACTH'\n" +
                    "      allow_operations : ['CREATE:*', 'DELETE:*', 'ALTER:*', 'ALTER_CONFIGS:*']\n" +
                    "\n" +
                    "    - name : 'group_two'\n" +
                    "      resource :\n" +
                    "        type : 'topic'\n" +
                    "        pattern : '/public-([.-])*/'\n" +
                    "        patternType : 'MACTH'\n" +
                    "      allow_operations : ['READ:*', 'WRITE:*']";

    private static final String ACL_USER_POLICIES =
            "acls:\n" +
                    "  access_policies:\n" +
                    "    - principal : 'principal_one'\n" +
                    "      groups    : ['group_one']\n" +
                    "  \n" +
                    "    - principal : 'principal_two'\n" +
                    "      groups: []\n" +
                    "      permissions :\n" +
                    "        - resource :\n" +
                    "            type : 'topic'\n" +
                    "            pattern : 'bench-'\n" +
                    "            patternType : 'PREFIXED'\n" +
                    "          allow_operations : ['READ:*', 'WRITE:*']";

    private  YAMLClusterSpecReader reader = new YAMLClusterSpecReader();

    @Test
    public void shouldReadEmptyFileWithCurrentVersion() {
        ClusterSpec specification = readTestSample(reader, "version: " + YAMLClusterSpecReader.CURRENT_VERSION);
        assertNotNull(specification);
    }

    @Test
    public void shouldReadEmptyFileWithNoInvalidVersion() {
        ClusterSpec specification = readTestSample(reader, "version: " + YAMLClusterSpecReader.CURRENT_VERSION.version());
        assertNotNull(specification);
    }

    @Test
    public void shouldReadMultipleAclGroupsGivenAValidInputYAML() {
        ClusterSpec specification = readTestSample(reader, ACL_GROUP_POLICIES);
        assertNotNull(specification);

        Map<String, AclGroupPolicy> policies = specification.getAclGroupPolicies();
        assertEquals(2, policies.size());
        AclGroupPolicy groupOne = policies.get("group_one");
        assertNotNull(groupOne);

        assertEquals("group_one", groupOne.name());
        assertEquals("/([.-])*/", groupOne.permission().pattern());
        assertEquals(4, groupOne.permission().operations().size());

        AclGroupPolicy groupTwo = policies.get("group_two");
        assertNotNull(groupTwo);
        assertEquals("group_two", groupTwo.name());
        assertEquals("/public-([.-])*/", groupTwo.permission().pattern());
        assertEquals(2, groupTwo.permission().operations().size());
    }

    @Test
    public void shouldReadMultipleAclUserGivenAValidInputYAML() {
        ClusterSpec specification = readTestSample(reader, ACL_USER_POLICIES);
        assertNotNull(specification);

        Collection<AclUserPolicy> policies = specification.getAclUsers();
        assertEquals(2, policies.size());
    }

    @Test
    public void shouldReadMultipleTopicResourcesGivenAValidInputYAML() {
        ClusterSpec specification = readTestSample(reader, THREE_TOPICS_SIMPLE_CONFIG);
        assertNotNull(specification);
        Collection<TopicResource> topics = specification.getTopics();
        assertNotNull(topics);
        assertEquals(3, topics.size());
        assertTrue(topics.contains(TOPIC_P1));
        assertTrue(topics.contains(TOPIC_P2));
        assertTrue(topics.contains(TOPIC_P3));
    }


    private ClusterSpec readTestSample(final YAMLClusterSpecReader reader, final String content) {
        return reader.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

}