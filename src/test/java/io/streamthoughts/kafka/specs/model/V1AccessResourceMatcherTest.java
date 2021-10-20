package io.streamthoughts.kafka.specs.model;

import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import org.apache.kafka.common.resource.PatternType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static io.streamthoughts.kafka.specs.resources.Named.keyByName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class V1AccessResourceMatcherTest {

    private static final String TEST_MODEL_ROLES_YAML = "test-model-roles.yaml";

    private final YAMLClusterSpecReader reader = new YAMLClusterSpecReader();

    @Test
    public void should_read_resource_with_default_pattern_type() {
        final V1SpecFile specFile = reader.read(getResourceAsStream(TEST_MODEL_ROLES_YAML), Collections.emptyMap());
        assertNotNull(specFile);

        final Map<String, V1AccessRoleObject> roles = keyByName(specFile.specs().security().get().roles());
        assertEquals(2, roles.size());
        V1AccessRoleObject roleOne = roles.get("role_one");
        assertNotNull(roleOne);

        assertEquals("role_one", roleOne.name());
        V1AccessPermission permOne = roleOne.permissions().iterator().next();
        assertEquals("my_topic", permOne.resource().pattern());
        assertEquals(PatternType.LITERAL, permOne.resource().patternType());
    }

    @Test
    public void should_read_resource_with_prefixed_pattern_type() {
        final V1SpecFile specFile = reader.read(getResourceAsStream(TEST_MODEL_ROLES_YAML), Collections.emptyMap());
        assertNotNull(specFile);

        final Map<String, V1AccessRoleObject> roles = keyByName(specFile.specs().security().get().roles());
        assertEquals(2, roles.size());
        V1AccessRoleObject roleTwo = roles.get("role_two");
        assertNotNull(roleTwo);

        assertEquals("role_two", roleTwo.name());
        V1AccessPermission permTwo = roleTwo.permissions().iterator().next();
        assertEquals("my_", permTwo.resource().pattern());
        assertEquals(PatternType.PREFIXED, permTwo.resource().patternType());
    }

    private static InputStream getResourceAsStream(final String resource) {
        return V1AccessResourceMatcherTest.class.getClassLoader().getResourceAsStream(resource);
    }
}