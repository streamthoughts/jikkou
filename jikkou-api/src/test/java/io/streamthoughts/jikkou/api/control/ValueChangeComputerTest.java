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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.change.ValueChangeComputer;
import io.streamthoughts.jikkou.api.model.GenericResource;
import io.streamthoughts.jikkou.api.model.GenericResourceChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueChangeComputerTest {

    public static class KeyValue extends GenericResource {
        public String key;
        public String value;

        KeyValue(String key, String value) {
            super(null, null, null, null);
            this.key = key;
            this.value = value;
        }
        public String key() { return  key; }
        public String value() { return  value; }

        @Override
        public String toString() {
            return "[" + key + ", " + value + ']';
        }
    }

    private static final ValueChangeComputer.ChangeValueMapper<KeyValue, KeyValue> VALUE_MAPPER =
            new ValueChangeComputer.ChangeValueMapper<>() {
        @NotNull
        @Override
        public KeyValue apply(@Nullable KeyValue before, @Nullable KeyValue after) {
            if (after != null) return after;
            if (before != null) return before;
            throw new IllegalArgumentException("both arguments are null");
        }
    };


    @Test
    void shouldComputeAddChangeForNonExistingObject() {
        // Given
        KeyValue after = new KeyValue("key", "v1");
        ValueChangeComputer<KeyValue, KeyValue> computer = new ValueChangeComputer<>(KeyValue::key, VALUE_MAPPER, false);

        // When
        List<HasMetadataChange<ValueChange<KeyValue>>> changes = computer.computeChanges(List.of(), List.of(after));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getChange().getChangeType());
        Assertions.assertEquals(after, changes.get(0).getChange().getAfter());
    }

    @Test
    void shouldComputeNoneChangeExistingObjectsGivenNoModification() {
        // Given
        KeyValue value = new KeyValue("key", "v1");
        ValueChangeComputer<KeyValue, KeyValue> computer = new ValueChangeComputer<>(KeyValue::key, VALUE_MAPPER, false);

        // When
        List<HasMetadataChange<ValueChange<KeyValue>>> changes = computer.computeChanges(List.of(value), List.of(value));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getChange().getChangeType());
        Assertions.assertEquals(value, changes.get(0).getChange().getBefore());
        Assertions.assertEquals(value, changes.get(0).getChange().getAfter());
    }

    @Test
    void shouldComputeUpdateChangeForExistingObjectsGivenModifications() {
        // Given
        KeyValue before = new KeyValue("key", "v1");
        KeyValue after = new KeyValue("key", "v2");
        ValueChangeComputer<KeyValue, KeyValue> computer = new ValueChangeComputer<>(KeyValue::key, VALUE_MAPPER, false);

        // When
        List<HasMetadataChange<ValueChange<KeyValue>>> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        List<GenericResourceChange<Change>> expected = List.of(GenericResourceChange
                .builder()
                        .withMetadata(null)
                        .withChange(ValueChange.with(before, after))
                .build()
        );
        Assertions.assertEquals(expected, changes);
    }

    @Test
    void shouldComputeNoChangeForUndefinedObjectAndDeleteOrphansFalse() {
        // Given
        KeyValue before = new KeyValue("key", "v1");
        ValueChangeComputer<KeyValue, KeyValue> computer = new ValueChangeComputer<>(KeyValue::key, VALUE_MAPPER, false);

        // When
        List<HasMetadataChange<ValueChange<KeyValue>>> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(0, changes.size());;
    }

    @Test
    void shouldComputeDeleteChangeForUndefinedObjectAndDeleteOrphansTrue() {
        // Given
        KeyValue before = new KeyValue("key", "v1");
        ValueChangeComputer<KeyValue, KeyValue> computer = new ValueChangeComputer<>(KeyValue::key, VALUE_MAPPER, true);

        // When
        List<HasMetadataChange<ValueChange<KeyValue>>> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getChange().getChangeType());
        Assertions.assertEquals(before, changes.get(0).getChange().getBefore());
        Assertions.assertNull(changes.get(0).getChange().getAfter());
    }
}