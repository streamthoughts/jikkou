/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change;

import io.streamthoughts.jikkou.aws.ApiVersions;
import io.streamthoughts.jikkou.aws.model.Compatibility;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchema;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchemaSpec;
import io.streamthoughts.jikkou.core.data.SchemaAndType;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AwsGlueSchemaChangeComputerTest {

    private final AwsGlueSchemaChangeComputer computer = new AwsGlueSchemaChangeComputer();

    @Test
    void shouldGetCreateChange() {
        // Given
        AwsGlueSchema schema = AwsGlueSchema
            .builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName("schema")
                .build()
            )
            .withSpec(AwsGlueSchemaSpec
                .builder()
                .withCompatibility(Compatibility.BACKWARD)
                .withDataFormat(SchemaType.AVRO)
                .withDescription("My Avro Schema")
                .withSchemaDefinition(new SchemaHandle("{}"))
                .build()
            )
            .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(schema));

        // Then
        GenericResourceChange change = GenericResourceChange
            .builder()
            .withApiVersion(ApiVersions.AWS_GLUE_API_VERSION)
            .withKind("AwsGlueSchemaChange")
            .withMetadata(ObjectMeta
                .builder()
                .withName("schema")
                .build()
            )
            .withSpec(new GenericResourceChangeSpec(
                Operation.CREATE,
                List.of(
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_SCHEMA, new SchemaAndType("{}", SchemaType.AVRO)),
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_FORMAT, SchemaType.AVRO),
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_SCHEMA_DESCRIPTION, "My Avro Schema"),
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_COMPATIBILITY, Compatibility.BACKWARD)
                ),
                Map.of()
            ))
            .build();
        Assertions.assertEquals(change, changes.getFirst());
    }

    @Test
    void shouldGetCreateChangeGivenNoDescription() {
        // Given
        AwsGlueSchema schema = AwsGlueSchema
            .builder()
            .withMetadata(ObjectMeta.builder().withName("schema").build())
            .withSpec(AwsGlueSchemaSpec
                .builder()
                .withCompatibility(Compatibility.BACKWARD)
                .withDataFormat(SchemaType.AVRO)
                .withSchemaDefinition(new SchemaHandle("{}"))
                .build()
            )
            .build();
        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(schema));

        // Then
        GenericResourceChange change = GenericResourceChange
            .builder()
            .withApiVersion(ApiVersions.AWS_GLUE_API_VERSION)
            .withKind("AwsGlueSchemaChange")
            .withMetadata(ObjectMeta.builder().withName("schema").build())
            .withSpec(new GenericResourceChangeSpec(
                Operation.CREATE,
                List.of(
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_SCHEMA, new SchemaAndType("{}", SchemaType.AVRO)),
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_FORMAT, SchemaType.AVRO),
                    StateChange.create(AwsGlueSchemaChangeComputer.DATA_COMPATIBILITY, Compatibility.BACKWARD)
                ),
                Map.of()
            ))
            .build();
        Assertions.assertEquals(change, changes.getFirst());
    }
}