package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.api.TestResource;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class ResourceTransformationChainTest {

    @Test
    void shouldRunTransformationsInPriorityOrder() {

        List<Integer> calls = new ArrayList<>();

        // Given
        ResourceTransformationChain chain = new ResourceTransformationChain(List.of(
                newTransformation(3, () -> calls.add(3)),
                newTransformation(0, () -> calls.add(0)),
                newTransformation(2, () -> calls.add(2)),
                newTransformation(1, () -> calls.add(1))
        ));
        // When
        chain.transform(new TestResource(), new GenericResourceListObject(Collections.emptyList()));

        // Then
        Assertions.assertEquals(4, calls.size());
        for (int i = 0; i < calls.size(); i++) {
            Assertions.assertEquals(i, calls.get(i));
        }
    }

    private ResourceTransformation<HasMetadata> newTransformation(int priority, Runnable onTransformation) {
        return new ResourceTransformation<>() {

            @Override
            public boolean canAccept(@NotNull ResourceType type) {
                return true;
            }

            @Override
            public boolean canAccept(@NotNull HasMetadata resource) {
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