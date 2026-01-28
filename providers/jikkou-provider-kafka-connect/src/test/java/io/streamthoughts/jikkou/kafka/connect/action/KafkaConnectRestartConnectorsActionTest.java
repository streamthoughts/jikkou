/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.action;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KafkaConnectRestartConnectorsAction}.
 */
class KafkaConnectRestartConnectorsActionTest {

    /**
     * Regression test for GitHub issue #536.
     * Verifies that CONNECT_CLUSTER config property has List as its raw type.
     * This ensures CLI correctly handles the option as a list type.
     *
     * @see <a href="https://github.com/streamthoughts/jikkou/issues/536">Issue #536</a>
     */
    @Test
    void shouldHaveListTypeForConnectClusterConfigProperty() {
        // GIVEN
        ConfigProperty<List<String>> property = KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER;

        // THEN
        Assertions.assertEquals(List.class, property.rawType(),
            "CONNECT_CLUSTER config property must have List as raw type to ensure CLI handles it correctly");
    }

    /**
     * Regression test for GitHub issue #536.
     * Verifies that CONNECTOR_NAME config property has List as its raw type.
     *
     * @see <a href="https://github.com/streamthoughts/jikkou/issues/536">Issue #536</a>
     */
    @Test
    void shouldHaveListTypeForConnectorNameConfigProperty() {
        // GIVEN
        ConfigProperty<List<String>> property = KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME;

        // THEN
        Assertions.assertEquals(List.class, property.rawType(),
            "CONNECTOR_NAME config property must have List as raw type");
    }

    /**
     * Regression test for GitHub issue #536.
     * Verifies that a single string value for connect-cluster is properly converted to a list.
     * This simulates the scenario where CLI passes --connect-cluster=example-cluster.
     *
     * @see <a href="https://github.com/streamthoughts/jikkou/issues/536">Issue #536</a>
     */
    @Test
    void shouldHandleSingleStringValueForConnectCluster() {
        // GIVEN - Configuration with a single string value (simulating CLI input)
        Configuration configuration = Configuration.from(Map.of(
            KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER.key(), "example-cluster"
        ));

        // WHEN
        List<String> result = KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER
            .getOptional(configuration)
            .orElse(null);

        // THEN - Should return a list containing the single value
        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(1, result.size(), "Result should contain exactly one element");
        Assertions.assertEquals("example-cluster", result.get(0), "Result should contain the single cluster name");
    }

    /**
     * Regression test for GitHub issue #536.
     * Verifies that a single string value for connector-name is properly converted to a list.
     *
     * @see <a href="https://github.com/streamthoughts/jikkou/issues/536">Issue #536</a>
     */
    @Test
    void shouldHandleSingleStringValueForConnectorName() {
        // GIVEN - Configuration with a single string value (simulating CLI input)
        Configuration configuration = Configuration.from(Map.of(
            KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME.key(), "my-connector"
        ));

        // WHEN
        List<String> result = KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME
            .getOptional(configuration)
            .orElse(null);

        // THEN - Should return a list containing the single value
        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(1, result.size(), "Result should contain exactly one element");
        Assertions.assertEquals("my-connector", result.get(0), "Result should contain the single connector name");
    }

    /**
     * Verifies that multiple values for connect-cluster are properly handled.
     */
    @Test
    void shouldHandleMultipleValuesForConnectCluster() {
        // GIVEN - Configuration with a list of values
        Configuration configuration = Configuration.from(Map.of(
            KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER.key(),
            List.of("cluster-1", "cluster-2", "cluster-3")
        ));

        // WHEN
        List<String> result = KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER
            .getOptional(configuration)
            .orElse(null);

        // THEN
        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(3, result.size(), "Result should contain all cluster names");
        Assertions.assertTrue(result.contains("cluster-1"));
        Assertions.assertTrue(result.contains("cluster-2"));
        Assertions.assertTrue(result.contains("cluster-3"));
    }

    /**
     * Verifies that multiple values for connector-name are properly handled.
     */
    @Test
    void shouldHandleMultipleValuesForConnectorName() {
        // GIVEN - Configuration with a list of values
        Configuration configuration = Configuration.from(Map.of(
            KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME.key(),
            List.of("connector-1", "connector-2")
        ));

        // WHEN
        List<String> result = KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME
            .getOptional(configuration)
            .orElse(null);

        // THEN
        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(2, result.size(), "Result should contain all connector names");
        Assertions.assertTrue(result.contains("connector-1"));
        Assertions.assertTrue(result.contains("connector-2"));
    }

    /**
     * Verifies that empty configuration returns empty Optional for connect-cluster.
     */
    @Test
    void shouldReturnEmptyOptionalForMissingConnectCluster() {
        // GIVEN - Empty configuration
        Configuration configuration = Configuration.empty();

        // WHEN
        var result = KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER
            .getOptional(configuration);

        // THEN
        Assertions.assertTrue(result.isEmpty(), "Result should be empty for missing configuration");
    }

    /**
     * Verifies that empty configuration returns empty Optional for connector-name.
     */
    @Test
    void shouldReturnEmptyOptionalForMissingConnectorName() {
        // GIVEN - Empty configuration
        Configuration configuration = Configuration.empty();

        // WHEN
        var result = KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME
            .getOptional(configuration);

        // THEN
        Assertions.assertTrue(result.isEmpty(), "Result should be empty for missing configuration");
    }

    /**
     * Verifies that configProperties() returns all expected config properties.
     */
    @Test
    void shouldReturnAllConfigProperties() {
        // GIVEN
        KafkaConnectRestartConnectorsAction action = new KafkaConnectRestartConnectorsAction();

        // WHEN
        var properties = action.configProperties();

        // THEN
        Assertions.assertEquals(4, properties.size(), "Should have 4 config properties");
        Assertions.assertTrue(properties.contains(KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME));
        Assertions.assertTrue(properties.contains(KafkaConnectRestartConnectorsAction.Config.CONNECT_CLUSTER));
        Assertions.assertTrue(properties.contains(KafkaConnectRestartConnectorsAction.Config.INCLUDE_TASKS));
        Assertions.assertTrue(properties.contains(KafkaConnectRestartConnectorsAction.Config.ONLY_FAILED));
    }
}