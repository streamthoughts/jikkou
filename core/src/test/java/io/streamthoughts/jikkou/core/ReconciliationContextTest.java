/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.selector.Selectors;
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
                NamedValueSet.setOf(new NamedValue("annotation", "value"))
        ), context);
    }

}