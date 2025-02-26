/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasConfigRefs;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigMapsTransformationTest {

    static final String TEST_CONFIG_MAP_NAME = "configMap";
    static final String TEST_CONFIG_K1 = "k1";
    static final String TEST_CONFIG_K2 = "k2";
    private final static ConfigMap TEST_CONFIG_MAP = ConfigMap
            .builder()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName(TEST_CONFIG_MAP_NAME)
                    .build())
            .withData(Map.of(TEST_CONFIG_K1, "v1"))
            .build();


    @Test
    void shouldAddConfigPropsForResourceWithNoPreviousConfigs() {
        // Given
        var resource = new TestResource(new TestConfigMapsObject(
                Configs.empty(),
                Set.of(TEST_CONFIG_MAP_NAME)
        ));

        // When
        var result = (TestResource) new ConfigMapsTransformation()
                .transform(resource, ResourceList.of(TEST_CONFIG_MAP), ReconciliationContext.Default.EMPTY)
                .get();

        // Then
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
    }

    @Test
    void shouldAddConfigPropsToTopicGivenValidConfigMapRefForTopicWithConfigs() {
        // Given
        var resource = new TestResource(new TestConfigMapsObject(
                Configs.of(TEST_CONFIG_K2, "v2"),
                Set.of(TEST_CONFIG_MAP_NAME)
        ));

        // When
        var result = (TestResource) new ConfigMapsTransformation()
                .transform(resource, ResourceList.of(TEST_CONFIG_MAP), ReconciliationContext.Default.EMPTY)
                .get();

        // Then
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals(2, configs.size());
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
        Assertions.assertEquals("v2", configs.get(TEST_CONFIG_K2).value());
    }

    @Test
    void shouldOverrideConfigPropsToTopicGivenValidConfigMapRefForTopicWithConfigs() {
        // Given
        var resource = new TestResource(new TestConfigMapsObject(
                Configs.of(TEST_CONFIG_K1, "v2"),
                Set.of(TEST_CONFIG_MAP_NAME)
        ));
        // When
        var result = (TestResource) new ConfigMapsTransformation()
                .transform(resource, ResourceList.of(TEST_CONFIG_MAP), ReconciliationContext.Default.EMPTY)
                .get();

        // Then
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals(1, configs.size());
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
    }

    public static class TestConfigMapsObject implements HasConfigRefs {

        private Configs configs;

        private Set<String> configMapRefs;

        public TestConfigMapsObject(Configs configs, Set<String> configMapRefs) {
            this.configs = configs;
            this.configMapRefs = configMapRefs;
        }

        @Override
        public Set<String> getConfigMapRefs() {
            return configMapRefs;
        }

        @Override
        public void setConfigMapRefs(Set<String> configMapsRefs) {
            this.configMapRefs = configMapsRefs;
        }

        @Override
        public Configs getConfigs() {
            return configs;
        }

        @Override
        public void setConfigs(final Configs configs) {
            this.configs = configs;
        }
    }

    public static class TestResource implements HasSpec<TestConfigMapsObject> {

        private final TestConfigMapsObject object;

        public TestResource(TestConfigMapsObject object) {
            this.object = object;
        }

        @Override
        public ObjectMeta getMetadata() {
            return null;
        }

        @Override
        public HasMetadata withMetadata(ObjectMeta metadata) {
            return null;
        }

        @Override
        public String getApiVersion() {
            return null;
        }

        @Override
        public String getKind() {
            return null;
        }

        @Override
        public TestConfigMapsObject getSpec() {
            return object;
        }
    }
}