/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ControllerTest {

    @Test
    void shouldGetSupportedModes() {
        Set<ReconciliationMode> modes = Controller.supportedReconciliationModes(TestController.class);
        Assertions.assertEquals(Set.of(ReconciliationMode.CREATE), modes);
    }

    @ControllerConfiguration(
            supportedModes = {ReconciliationMode.CREATE}
    )
    static abstract class TestController<
            R extends HasMetadata> implements Controller<R> {

    }
}