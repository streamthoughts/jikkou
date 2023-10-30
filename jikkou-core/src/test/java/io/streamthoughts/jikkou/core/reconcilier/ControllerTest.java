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
package io.streamthoughts.jikkou.core.reconcilier;

import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
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
            R extends HasMetadata,
            C extends Change> implements Controller<R , C> {

    }
}