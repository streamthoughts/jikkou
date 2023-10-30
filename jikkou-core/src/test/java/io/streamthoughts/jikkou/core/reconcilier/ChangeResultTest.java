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

import io.streamthoughts.jikkou.core.models.DefaultResourceChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeResultTest {

    HasMetadataChange<Change> change = DefaultResourceChange.builder().withChange(() -> ChangeType.NONE).build();

    ChangeDescription description = () -> "test";

    @Test
    void shouldCreateChangeForOk() {
        // When
        ChangeResult<Change> result = DefaultChangeResult.ok(
                change,
                description
        );

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isChanged());
    }

    @Test
    void shouldCreateChangeForChanged() {
        // When
        ChangeResult<Change> result = DefaultChangeResult.changed(
                change,
                description
        );

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isChanged());
    }

    @Test
    void shouldCreateChangeForFailed() {
        // When
        ChangeResult<Change> result = DefaultChangeResult.failed(change,
                description,
                List.of(new ChangeError("Failed"))
       );

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isChanged());
    }

}