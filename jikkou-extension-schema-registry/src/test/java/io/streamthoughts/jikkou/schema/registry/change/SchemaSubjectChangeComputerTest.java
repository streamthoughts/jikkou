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

import io.streamthoughts.jikkou.common.utils.Json;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
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

    private final SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

    @Test
    void shouldGetAddChangeForNewSubject() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-subject")
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<SchemaSubjectChange> changes = computer.computeChanges(List.of(), List.of(resource))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).operation());
    }

    @Test
    void shouldGetNoneChangeForExistingSubjectGivenNoChange() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-subject")
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<SchemaSubjectChange> changes = computer.computeChanges(List.of(resource), List.of(resource))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).operation());
    }

    @Test
    void shouldGetUpdateChangeForExistingSubjectGivenUpdatedCompatibility() {
        // Given
        V1SchemaRegistrySubject before = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-subject")
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
                        .withName("test-subject")
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withCompatibilityLevel(CompatibilityLevels.BACKWARD)
                        .withSchema(new SchemaHandle(SCHEMA_V1.toString()))
                        .build())
                .build();
        // When
        List<SchemaSubjectChange> changes = computer.computeChanges(List.of(before), List.of(after))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.UPDATE, changes.get(0).operation());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getCompatibilityLevels().operation());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getSchema().operation());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getSchemaType().operation());

        Assertions.assertNull(changes.get(0).getCompatibilityLevels().getBefore());
        Assertions.assertEquals(CompatibilityLevels.BACKWARD, changes.get(0).getCompatibilityLevels().getAfter());
    }

    @Test
    void shouldGetUpdateChangeForExistingSubjectGivenUpdatedSchema() {
        // Given
        V1SchemaRegistrySubject before = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-subject")
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
                        .withName("test-subject")
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(SCHEMA_V2.toString()))
                        .build())
                .build();
        // When
        List<SchemaSubjectChange> changes = computer.computeChanges(List.of(before), List.of(after))
                .stream().map(HasMetadataChange::getChange).toList();
        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.UPDATE, changes.get(0).operation());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getCompatibilityLevels().operation());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getSchemaType().operation());

        ValueChange<String> schemaChange = changes.get(0).getSchema();
        Assertions.assertEquals(ChangeType.UPDATE, schemaChange.operation());
        Assertions.assertNull(changes.get(0).getCompatibilityLevels().getBefore());

        Assertions.assertEquals(Json.normalize(SCHEMA_V2.toString()), schemaChange.getAfter());
        Assertions.assertEquals(Json.normalize(SCHEMA_V1.toString()), schemaChange.getBefore());
    }
}