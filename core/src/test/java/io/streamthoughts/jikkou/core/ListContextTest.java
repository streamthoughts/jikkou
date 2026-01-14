/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListContextTest {

    @Test
    void shouldBuildListContextWithAllFields() {
        // Given
        Configuration configuration = Configuration.of("key", "value");
        String providerName = "test-provider";

        // When
        ListContext context = ListContext.builder()
                .selector(Selectors.NO_SELECTOR)
                .configuration(configuration)
                .providerName(providerName)
                .build();

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldBuildListContextWithDefaultValues() {
        // When
        ListContext context = ListContext.builder().build();

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(Configuration.empty(), context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldBuildListContextWithOnlySelector() {
        // When
        ListContext context = ListContext.builder()
                .selector(Selectors.NO_SELECTOR)
                .build();

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(Configuration.empty(), context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldBuildListContextWithOnlyConfiguration() {
        // Given
        Configuration configuration = Configuration.of("key", "value");

        // When
        ListContext context = ListContext.builder()
                .configuration(configuration)
                .build();

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldBuildListContextWithOnlyProviderName() {
        // Given
        String providerName = "test-provider";

        // When
        ListContext context = ListContext.builder()
                .providerName(providerName)
                .build();

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(Configuration.empty(), context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldBuildListContextUsingChainedBuilderCalls() {
        // Given
        Configuration config1 = Configuration.of("key1", "value1");
        Configuration config2 = Configuration.of("key2", "value2");

        // When - each builder call returns a new builder
        ListContext.Builder builder1 = ListContext.builder();
        ListContext.Builder builder2 = builder1.configuration(config1);
        ListContext.Builder builder3 = builder2.configuration(config2);
        ListContext.Builder builder4 = builder3.providerName("provider1");
        ListContext.Builder builder5 = builder4.providerName("provider2");

        ListContext context = builder5.build();

        // Then - last values should win
        Assertions.assertEquals(config2, context.configuration());
        Assertions.assertEquals("provider2", context.providerName());
    }

    @Test
    void shouldHaveEmptyDefaultInstance() {
        // When
        ListContext emptyContext = ListContext.Default.EMPTY;

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, emptyContext.selector());
        Assertions.assertEquals(Configuration.empty(), emptyContext.configuration());
        Assertions.assertNull(emptyContext.providerName());
    }

    @Test
    void shouldCreateListContextRecord() {
        // Given
        Configuration configuration = Configuration.of("key", "value");
        String providerName = "test-provider";

        // When
        ListContext.Default context = new ListContext.Default(
                Selectors.NO_SELECTOR,
                configuration,
                providerName
        );

        // Then
        Assertions.assertEquals(Selectors.NO_SELECTOR, context.selector());
        Assertions.assertEquals(configuration, context.configuration());
        Assertions.assertEquals(providerName, context.providerName());
    }

    @Test
    void shouldSupportNullProviderName() {
        // When
        ListContext context = ListContext.builder()
                .providerName(null)
                .build();

        // Then
        Assertions.assertNull(context.providerName());
    }
}
