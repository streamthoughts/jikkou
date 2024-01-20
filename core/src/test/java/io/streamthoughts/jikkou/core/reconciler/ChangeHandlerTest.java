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
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeHandlerTest {

    @Test
    void shouldGetDefaultChangeHandler() {
        // Given
        ChangeHandler.None<ResourceChange> handler = new ChangeHandler.None<>(change -> () -> "NONE");
        ResourceChange change = GenericResourceChange.builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .build()
                )
                .build();

        // When / Then
        Set<Operation> supportedOperations = handler.supportedChangeTypes();
        Assertions.assertEquals(1, supportedOperations.size());
        Assertions.assertEquals(Operation.NONE, handler.supportedChangeTypes().iterator().next());

        // When
        List<ChangeResponse<ResourceChange>> results = handler.handleChanges(List.of(change));

        // Then
        List<ChangeResponse<ResourceChange>> expected = List.of(new ChangeResponse<>(change));
        Assertions.assertEquals(expected, results);

        // When / Then
        TextDescription description = handler.describe(change);
        Assertions.assertNotNull(description);
        Assertions.assertEquals("NONE", description.textual());
    }
}