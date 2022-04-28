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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.kafka.error.InvalidSpecsFileException;
import io.streamthoughts.jikkou.kafka.io.YAMLSpecReader;
import io.streamthoughts.jikkou.kafka.model.V1AccessPermission;
import io.streamthoughts.jikkou.kafka.model.V1AccessRoleObject;
import io.streamthoughts.jikkou.kafka.model.V1AccessUserObject;
import io.streamthoughts.jikkou.kafka.model.V1SpecFile;
import io.streamthoughts.jikkou.kafka.model.V1TopicObject;
import io.streamthoughts.jikkou.kafka.resources.ConfigValue;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static io.streamthoughts.jikkou.kafka.resources.Named.keyByName;
import static org.junit.jupiter.api.Assertions.*;

public class YAMLClusterSpecReaderTest {

    private static final V1TopicObject TOPIC_P1 = new V1TopicObject("my-topic-p1", 1, (short)1);

    private static final V1TopicObject TOPIC_P2 = new V1TopicObject("my-topic-p2", 2, (short)1)
            .addConfigValue(new ConfigValue(TopicConfig.RETENTION_MS_CONFIG, "10000"))
            .addConfigMapRef("CleanupPolicy");

    private static final V1TopicObject TOPIC_P3 = new V1TopicObject("my-topic-p3", null, null);

    private static final Map<String, Object> EMPTY_LABELS = Collections.emptyMap();
    private static final Map<String, Object> EMPTY_VARS = Collections.emptyMap();

    private static final String TEST_MODEL_TOPICS_YAML = "test-model-topics.yaml";
    private static final String TEST_ACLS_USERS_YAML = "test-model-security-users.yaml";
    private static final String TEST_ACLS_ROLES_YAML = "test-model-security-roles.yaml";

    private final YAMLSpecReader reader = new YAMLSpecReader();

    @Test
    public void should_fail_given_empty_file_using_current_version() {
        final InvalidSpecsFileException e = Assertions.assertThrows(InvalidSpecsFileException.class, () -> {
            readTestSample(reader, "");
        });
        assertEquals("Empty specification file", e.getLocalizedMessage());
    }

    @Test
    public void should_read_specification_given_empty_file_with_current_version() {
        final V1SpecFile specification = readTestSample(reader, "version: " + YAMLSpecReader.CURRENT_VERSION.version());
        assertNotNull(specification);
    }

    @Test
    public void should_read_multiple_acl_roles_given_valid_YAML() {
        final V1SpecFile specFile = reader.read(getResourceAsStream(TEST_ACLS_ROLES_YAML), EMPTY_VARS, EMPTY_LABELS);
        assertNotNull(specFile);

        final Map<String, V1AccessRoleObject> policies = keyByName(specFile.specs().security().get().roles());
        assertEquals(2, policies.size());
        V1AccessRoleObject groupOne = policies.get("group_one");
        assertNotNull(groupOne);

        assertEquals("group_one", groupOne.name());
        assertEquals(1, groupOne.permissions().size());
        V1AccessPermission permOne = groupOne.permissions().iterator().next();
        assertEquals("/([.-])*/", permOne.resource().pattern());
        assertEquals(4, permOne.operations().size());

        V1AccessRoleObject groupTwo = policies.get("group_two");
        assertNotNull(groupTwo);
        assertEquals("group_two", groupTwo.name());
        assertEquals(2, groupTwo.permissions().size());
        Iterator<V1AccessPermission> permTwoIter = groupTwo.permissions().iterator();
        V1AccessPermission permTwo = permTwoIter.next();
        assertEquals("/public-([.-])*/", permTwo.resource().pattern());
        assertEquals(2, permTwo.operations().size());
        permTwo = permTwoIter.next();
        assertEquals("public-", permTwo.resource().pattern());
        assertEquals(1, permTwo.operations().size());
    }

    @Test
    public void should_read_multiple_acl_access_given_valid_YAML() {
        final V1SpecFile specFile = reader.read(getResourceAsStream(TEST_ACLS_USERS_YAML), EMPTY_VARS, EMPTY_LABELS);
        assertNotNull(specFile);
        Collection<V1AccessUserObject> policies = specFile.specs().security().get().users();
        assertEquals(2, policies.size());
    }

    @Test
    public void should_read_multiple_topics_given_valid_YAML() {
        final V1SpecFile specFile = reader.read(getResourceAsStream(TEST_MODEL_TOPICS_YAML), EMPTY_VARS, EMPTY_LABELS);
        assertNotNull(specFile);
        Collection<V1TopicObject> topics = specFile.specs().topics();
        assertNotNull(topics);
        assertEquals(3, topics.size());
        assertTrue(topics.contains(TOPIC_P1), "should contain: " + TOPIC_P1);
        assertTrue(topics.contains(TOPIC_P2), "should contain: " + TOPIC_P2);
        assertTrue(topics.contains(TOPIC_P3), "should contain: " + TOPIC_P3);
    }

    private static InputStream getResourceAsStream(final String resource) {
        return YAMLClusterSpecReaderTest.class.getClassLoader().getResourceAsStream(resource);
    }

    private V1SpecFile readTestSample(final YAMLSpecReader reader, final String content) {
        return reader.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), EMPTY_VARS, EMPTY_LABELS);
    }
}