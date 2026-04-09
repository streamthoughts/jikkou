/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.internals;

import io.jikkou.iceberg.IcebergAnnotations;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.view.models.V1IcebergView;
import io.jikkou.iceberg.view.models.V1IcebergViewQuery;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.SQLViewRepresentation;
import org.apache.iceberg.view.View;
import org.apache.iceberg.view.ViewVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IcebergViewConverterTest {

    @Test
    void shouldConvertSimpleView() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get(), "User name")
        );

        SQLViewRepresentation sparkRepr = ImmutableSQLViewRepresentation.builder()
            .sql("SELECT id, name FROM db.users")
            .dialect("spark")
            .build();

        ViewVersion version = Mockito.mock(ViewVersion.class);
        Mockito.when(version.representations()).thenReturn(List.of(sparkRepr));
        Mockito.when(version.defaultNamespace()).thenReturn(Namespace.of("db"));
        Mockito.when(version.defaultCatalog()).thenReturn("my_catalog");

        View view = Mockito.mock(View.class);
        Mockito.when(view.schema()).thenReturn(schema);
        Mockito.when(view.currentVersion()).thenReturn(version);
        Mockito.when(view.properties()).thenReturn(Map.of("comment", "test view"));
        Mockito.when(view.location()).thenReturn("s3://bucket/db/my_view");

        TableIdentifier identifier = TableIdentifier.of("db", "my_view");

        V1IcebergView result = IcebergViewConverter.toV1IcebergView(view, identifier);

        // Metadata
        Assertions.assertEquals("db.my_view", result.getMetadata().getName());
        Assertions.assertEquals("s3://bucket/db/my_view",
            result.getMetadata().getAnnotations().get(IcebergAnnotations.VIEW_LOCATION));

        // Schema
        Assertions.assertNotNull(result.getSpec().getSchema());
        Assertions.assertEquals(2, result.getSpec().getSchema().getColumns().size());

        V1IcebergColumn idCol = result.getSpec().getSchema().getColumns().get(0);
        Assertions.assertEquals("id", idCol.getName());
        Assertions.assertEquals("long", idCol.getType());
        Assertions.assertTrue(idCol.getRequired());

        V1IcebergColumn nameCol = result.getSpec().getSchema().getColumns().get(1);
        Assertions.assertEquals("name", nameCol.getName());
        Assertions.assertEquals("string", nameCol.getType());
        Assertions.assertFalse(nameCol.getRequired());
        Assertions.assertEquals("User name", nameCol.getDoc());

        // Queries
        Assertions.assertEquals(1, result.getSpec().getQueries().size());
        V1IcebergViewQuery query = result.getSpec().getQueries().get(0);
        Assertions.assertEquals("spark", query.getDialect());
        Assertions.assertEquals("SELECT id, name FROM db.users", query.getSql());

        // Defaults
        Assertions.assertEquals("db", result.getSpec().getDefaultNamespace());
        Assertions.assertEquals("my_catalog", result.getSpec().getDefaultCatalog());

        // Properties
        Assertions.assertEquals("test view", result.getSpec().getProperties().get("comment"));
    }

    @Test
    void shouldConvertViewWithMultipleDialects() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "count", Types.LongType.get())
        );

        SQLViewRepresentation sparkRepr = ImmutableSQLViewRepresentation.builder()
            .sql("SELECT count(*) AS count FROM t")
            .dialect("spark")
            .build();

        SQLViewRepresentation trinoRepr = ImmutableSQLViewRepresentation.builder()
            .sql("SELECT count(*) AS count FROM t")
            .dialect("trino")
            .build();

        ViewVersion version = Mockito.mock(ViewVersion.class);
        Mockito.when(version.representations()).thenReturn(List.of(sparkRepr, trinoRepr));
        Mockito.when(version.defaultNamespace()).thenReturn(Namespace.of("db"));
        Mockito.when(version.defaultCatalog()).thenReturn(null);

        View view = Mockito.mock(View.class);
        Mockito.when(view.schema()).thenReturn(schema);
        Mockito.when(view.currentVersion()).thenReturn(version);
        Mockito.when(view.properties()).thenReturn(Map.of());
        Mockito.when(view.location()).thenReturn("s3://bucket/db/v");

        V1IcebergView result = IcebergViewConverter.toV1IcebergView(
            view, TableIdentifier.of("db", "v"));

        Assertions.assertEquals(2, result.getSpec().getQueries().size());
        Assertions.assertEquals("spark", result.getSpec().getQueries().get(0).getDialect());
        Assertions.assertEquals("trino", result.getSpec().getQueries().get(1).getDialect());
        Assertions.assertNull(result.getSpec().getDefaultCatalog());
        Assertions.assertNull(result.getSpec().getProperties());
    }

    @Test
    void shouldOmitSchemaWhenColumnsAreEmpty() {
        Schema schema = new Schema(); // empty schema — no columns

        SQLViewRepresentation repr = ImmutableSQLViewRepresentation.builder()
            .sql("SELECT * FROM t")
            .dialect("spark")
            .build();

        ViewVersion version = Mockito.mock(ViewVersion.class);
        Mockito.when(version.representations()).thenReturn(List.of(repr));
        Mockito.when(version.defaultNamespace()).thenReturn(Namespace.of("db"));
        Mockito.when(version.defaultCatalog()).thenReturn(null);

        View view = Mockito.mock(View.class);
        Mockito.when(view.schema()).thenReturn(schema);
        Mockito.when(view.currentVersion()).thenReturn(version);
        Mockito.when(view.properties()).thenReturn(Map.of());
        Mockito.when(view.location()).thenReturn("s3://bucket/db/v");

        V1IcebergView result = IcebergViewConverter.toV1IcebergView(
            view, TableIdentifier.of("db", "v"));

        Assertions.assertNull(result.getSpec().getSchema());
    }
}
