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

import io.streamthoughts.jikkou.core.models.GenericResourceChange;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeHandlerTest {

    @Test
    void shouldGetDefaultChangeHandler() {
        // Given
        ChangeHandler.None<Change> handler = new ChangeHandler.None<>(change -> () -> "NONE");
        Change change = () -> ChangeType.NONE;

        // When / Then
        Set<ChangeType> supportedChangeTypes = handler.supportedChangeTypes();
        Assertions.assertEquals(1, supportedChangeTypes.size());
        Assertions.assertEquals(ChangeType.NONE, handler.supportedChangeTypes().iterator().next());

        // When
        GenericResourceChange<Change> input = GenericResourceChange
                .builder()
                .withChange(change)
                .build();

        List<ChangeResponse<Change>> result = handler.apply(List.of(input));

        // Then
        List<ChangeResponse<Change>> expected = List.of(new ChangeResponse<>(input));
        Assertions.assertEquals(expected, result);

        // When / Then
        ChangeDescription description = handler.getDescriptionFor(input);
        Assertions.assertNotNull(description);
        Assertions.assertEquals("NONE", description.textual());
    }
}