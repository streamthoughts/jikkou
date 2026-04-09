/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import io.jikkou.core.config.Configuration;
import io.jikkou.core.models.NamedValue;
import io.jikkou.core.models.NamedValueSet;
import io.jikkou.core.selector.Selectors;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReconciliationContextTest {

    @Test
    void shouldBuildReconciliationContext() {
        ReconciliationContext context = ReconciliationContext
                .builder()
                .dryRun(true)
                .label(new NamedValue("label", "value"))
                .annotation(new NamedValue("annotation", "value"))
                .selector(Selectors.NO_SELECTOR)
                .configuration(Configuration.empty())
                .build();

        Assertions.assertEquals(new ReconciliationContext.Default(
                Selectors.NO_SELECTOR,
                Configuration.empty(),
                true,
                NamedValueSet.setOf(new NamedValue("label", "value")),
                NamedValueSet.setOf(new NamedValue("annotation", "value")),
                null,
                List.of(),
                false
        ), context);
    }

    @Test
    void shouldBuildContextWithProviderNames() {
        ReconciliationContext context = ReconciliationContext.builder()
            .providerNames(List.of("kafka-prod", "kafka-staging"))
            .build();

        Assertions.assertEquals(List.of("kafka-prod", "kafka-staging"), context.providerNames());
        Assertions.assertTrue(context.isMultiProvider());
    }

    @Test
    void shouldBuildContextWithContinueOnError() {
        ReconciliationContext context = ReconciliationContext.builder()
            .providerNames(List.of("kafka-prod", "kafka-staging"))
            .continueOnError(true)
            .build();

        Assertions.assertTrue(context.continueOnError());
        Assertions.assertTrue(context.isMultiProvider());
    }

    @Test
    void shouldDefaultToEmptyProviderNames() {
        ReconciliationContext context = ReconciliationContext.builder().build();

        Assertions.assertTrue(context.providerNames().isEmpty());
        Assertions.assertFalse(context.isMultiProvider());
        Assertions.assertFalse(context.continueOnError());
    }

    @Test
    void shouldBuildSingleProviderContext() {
        ReconciliationContext context = ReconciliationContext.builder()
            .providerName("kafka-prod")
            .build();

        Assertions.assertEquals("kafka-prod", context.providerName());
        Assertions.assertFalse(context.isMultiProvider());
    }

    @Test
    void shouldPreserveBothSingleAndMultiProviderFields() {
        ReconciliationContext context = ReconciliationContext.builder()
            .providerName("kafka-prod")
            .providerNames(List.of("kafka-staging", "kafka-dev"))
            .continueOnError(true)
            .dryRun(false)
            .build();

        Assertions.assertEquals("kafka-prod", context.providerName());
        Assertions.assertEquals(List.of("kafka-staging", "kafka-dev"), context.providerNames());
        Assertions.assertTrue(context.continueOnError());
        Assertions.assertFalse(context.isDryRun());
        Assertions.assertTrue(context.isMultiProvider());
    }

    @Test
    void shouldEmptyContextNotBeMultiProvider() {
        Assertions.assertFalse(ReconciliationContext.Default.EMPTY.isMultiProvider());
        Assertions.assertFalse(ReconciliationContext.Default.EMPTY.continueOnError());
    }
}
