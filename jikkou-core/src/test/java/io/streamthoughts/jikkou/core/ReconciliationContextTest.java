/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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