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
package io.streamthoughts.jikkou.schema.registry.change;

import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_REFERENCES;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA_TYPE;

import io.streamthoughts.jikkou.core.data.json.Json;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaSubjectChangeComputerTest {

    private static final Schema SCHEMA_V1 = SchemaBuilder
            .record("test")
            .fields()
            .optionalString("fieldA")
            .optionalString("fieldB")
            .endRecord();

    private static final Schema SCHEMA_V2 = SchemaBuilder
            .record("test")
            .fields()
            .optionalString("fieldA")
            .optionalString("fieldB")
            .optionalString("fieldC")
            .endRecord();
    public static final String TEST_SUBJECT = "test-subject";

    private final SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

    @Test
    void shouldGetAddChangeForNewSubject() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(resource));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1SchemaRegistrySubject.class)
                        .withMetadata(ObjectMeta
                                .builder()
                                .withName(TEST_SUBJECT)
                                .build()
                        )
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.CREATE)
                                .withData(Map.of(
                                        "permanentDelete", false,
                                        "normalizeSchema", false
                                ))
                                .withChange(StateChange.create(DATA_COMPATIBILITY_LEVEL, null))
                                .withChange(StateChange.create(DATA_SCHEMA, SCHEMA_V1.toString()))
                                .withChange(StateChange.create(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                                .withChange(StateChange.create(DATA_REFERENCES, Collections.emptyList()))
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, changes);
    }

    @Test
    void shouldGetNoneChangeForExistingSubjectGivenNoChange() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(resource), List.of(resource));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1SchemaRegistrySubject.class)
                        .withMetadata(ObjectMeta
                                .builder()
                                .withName(TEST_SUBJECT)
                                .build()
                        )
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.NONE)
                                .withData(Map.of(
                                        "permanentDelete", false,
                                        "normalizeSchema", false
                                ))
                                .withChange(StateChange.none(DATA_COMPATIBILITY_LEVEL, null))
                                .withChange(StateChange.none(DATA_SCHEMA, Json.normalize(SCHEMA_V1.toString())))
                                .withChange(StateChange.none(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                                .withChange(StateChange.none(DATA_REFERENCES, Collections.emptyList()))
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, changes);
    }

    @Test
    void shouldGetUpdateChangeForExistingSubjectGivenUpdatedCompatibility() {
        // Given
        V1SchemaRegistrySubject before = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();

        V1SchemaRegistrySubject after = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withCompatibilityLevel(CompatibilityLevels.BACKWARD)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1SchemaRegistrySubject.class)
                        .withMetadata(ObjectMeta
                                .builder()
                                .withName(TEST_SUBJECT)
                                .build()
                        )
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.UPDATE)
                                .withData(Map.of(
                                        "permanentDelete", false,
                                        "normalizeSchema", false
                                ))
                                .withChange(StateChange.create(DATA_COMPATIBILITY_LEVEL, CompatibilityLevels.BACKWARD))
                                .withChange(StateChange.none(DATA_SCHEMA, Json.normalize(SCHEMA_V1.toString())))
                                .withChange(StateChange.none(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                                .withChange(StateChange.none(DATA_REFERENCES, Collections.emptyList()))
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, changes);
    }

    @Test
    void shouldGetUpdateChangeForExistingSubjectGivenUpdatedSchema() {
        // Given
        V1SchemaRegistrySubject before = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();

        V1SchemaRegistrySubject after = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V2.toString()))
                        .build())
                .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));
        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange
                        .builder(V1SchemaRegistrySubject.class)
                        .withMetadata(ObjectMeta
                                .builder()
                                .withName(TEST_SUBJECT)
                                .build()
                        )
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.UPDATE)
                                .withData(Map.of(
                                        "permanentDelete", false,
                                        "normalizeSchema", false
                                ))
                                .withChange(StateChange.none(DATA_COMPATIBILITY_LEVEL, null))
                                .withChange(StateChange.none(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                                .withChange(StateChange.update(
                                        DATA_SCHEMA,
                                        Json.normalize(SCHEMA_V1.toString()),
                                        Json.normalize(SCHEMA_V2.toString()))
                                )
                                .withChange(StateChange.none(DATA_REFERENCES, Collections.emptyList()))
                                .build()
                        )
                        .build()
        );
        Assertions.assertEquals(expected, changes);
    }
}