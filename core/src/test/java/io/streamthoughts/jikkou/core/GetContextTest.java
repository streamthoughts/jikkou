/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GetContextTest {

    @Test
    void shouldBuildGetContextWithAllFields() {
        // Given
        Configuration configuration = Configuration.of("key", "value");
        String providerName = "test-provider";

        // When
        GetContext context = GetContext.builder()
                .configuration(configuration)
                .providerName(providerName)
                .build();

        // Then
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldBuildGetContextWithDefaultValues() {
        // When
        GetContext context = GetContext.builder().build();

        // Then
        Assertions.assertEquals(Configuration.empty(), context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldBuildGetContextWithOnlyConfiguration() {
        // Given
        Configuration configuration = Configuration.of("key", "value");

        // When
        GetContext context = GetContext.builder()
                .configuration(configuration)
                .build();

        // Then
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldBuildGetContextWithOnlyProviderName() {
        // Given
        String providerName = "test-provider";

        // When
        GetContext context = GetContext.builder()
                .providerName(providerName)
                .build();

        // Then
        Assertions.assertEquals(Configuration.empty(), context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldBuildGetContextUsingChainedBuilderCalls() {
        // Given
        Configuration config1 = Configuration.of("key1", "value1");
        Configuration config2 = Configuration.of("key2", "value2");

        // When - each builder call returns a new builder
        GetContext.Builder builder1 = GetContext.builder();
        GetContext.Builder builder2 = builder1.configuration(config1);
        GetContext.Builder builder3 = builder2.configuration(config2);
        GetContext.Builder builder4 = builder3.providerName("provider1");
        GetContext.Builder builder5 = builder4.providerName("provider2");

        GetContext context = builder5.build();

        // Then - last values should win
        Assertions.assertEquals(config2, context.configuration());
        Assertions.assertEquals("provider2", context.providerName());
    }

    @Test
    void shouldHaveEmptyDefaultInstance() {
        // When
        GetContext emptyContext = GetContext.Default.EMPTY;

        // Then
        Assertions.assertEquals(Configuration.empty(), emptyContext.configuration());
        Assertions.assertNull(emptyContext.providerName());
    }

    @Test
    void shouldCreateGetContextRecord() {
        // Given
        Configuration configuration = Configuration.of("key", "value");
        String providerName = "test-provider";

        // When
        GetContext.Default context = new GetContext.Default(
                configuration,
                providerName
        );

        // Then
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldSupportNullProviderName() {
        // When
        GetContext context = GetContext.builder()
                .providerName(null)
                .build();

        // Then
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldSupportComplexConfiguration() {
        // Given
        Configuration configuration = Configuration.from(java.util.Map.of(
                "bootstrap.servers", "localhost:9092",
                "security.protocol", "SASL_SSL",
                "nested", java.util.Map.of("key1", "value1", "key2", "value2")
        ));

        // When
        GetContext context = GetContext.builder()
                .configuration(configuration)
                .providerName("kafka-prod")
                .build();

        // Then
        Assertions.assertEquals("localhost:9092", context.configuration().getString("bootstrap.servers"));
        Assertions.assertEquals("SASL_SSL", context.configuration().getString("security.protocol"));
        Assertions.assertEquals("kafka-prod", context.providerName());
    }
}
