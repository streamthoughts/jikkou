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
package io.streamthoughts.jikkou.core.resource.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.transform.TransformationChain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTransformationChainTest {

    @Test
    void shouldRunTransformationsInPriorityOrder() {

        List<Integer> calls = new ArrayList<>();

        // Given
        TransformationChain chain = new TransformationChain(List.of(
                newTransformation(3, () -> calls.add(3)),
                newTransformation(0, () -> calls.add(0)),
                newTransformation(2, () -> calls.add(2)),
                newTransformation(1, () -> calls.add(1))
        ));
        // When
        chain.transform(
                new TestResource(),
                new DefaultResourceListObject<>(Collections.emptyList()),
                ReconciliationContext.Default.EMPTY
        );

        // Then
        Assertions.assertEquals(List.of(0, 1, 2, 3), calls);
    }

    private Transformation<HasMetadata> newTransformation(int priority, Runnable onTransformation) {
        return new Transformation<>() {

            @Override
            public boolean canAccept(@NotNull ResourceType type) {
                return true;
            }
            
            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform,
                                                            @NotNull HasItems resources) {
                onTransformation.run();
                return Optional.of(toTransform);
            }
        };
    }
}