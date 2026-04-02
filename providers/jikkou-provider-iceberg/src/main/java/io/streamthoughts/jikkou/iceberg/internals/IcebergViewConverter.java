/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.internals;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.iceberg.IcebergAnnotations;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergColumn;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergSchema;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergView;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergViewQuery;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergViewSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.view.SQLViewRepresentation;
import org.apache.iceberg.view.View;
import org.apache.iceberg.view.ViewRepresentation;
import org.apache.iceberg.view.ViewVersion;
import org.jetbrains.annotations.NotNull;

/**
 * Converts Apache Iceberg {@link View} objects to {@link V1IcebergView} resources.
 */
public final class IcebergViewConverter {

    private IcebergViewConverter() {
        // utility class
    }

    /**
     * Converts an Iceberg {@link View} to a {@link V1IcebergView} resource.
     *
     * @param view       the Iceberg view.
     * @param identifier the view identifier (namespace + name).
     * @return the corresponding resource.
     */
    @NotNull
    public static V1IcebergView toV1IcebergView(@NotNull final View view,
                                                 @NotNull final TableIdentifier identifier) {
        String resourceName = identifier.toString();
        ViewVersion currentVersion = view.currentVersion();

        // Convert schema (null when empty — schema is engine-inferred, not available via catalog API)
        V1IcebergSchema schema = convertSchema(view.schema());
        if (schema.getColumns() == null || schema.getColumns().isEmpty()) {
            schema = null;
        }

        // Convert SQL representations to queries
        List<V1IcebergViewQuery> queries = new ArrayList<>();
        for (ViewRepresentation representation : currentVersion.representations()) {
            if (representation instanceof SQLViewRepresentation sqlRepr) {
                queries.add(V1IcebergViewQuery.builder()
                    .withSql(sqlRepr.sql())
                    .withDialect(sqlRepr.dialect())
                    .build());
            }
        }

        // Extract default namespace
        Namespace defaultNs = currentVersion.defaultNamespace();
        String defaultNamespace = defaultNs != null && defaultNs.length() > 0
            ? String.join(".", defaultNs.levels())
            : null;

        // Extract default catalog
        String defaultCatalog = currentVersion.defaultCatalog();

        // Extract properties
        Map<String, String> properties = view.properties();

        V1IcebergViewSpec spec = new V1IcebergViewSpec();
        spec.setSchema(schema);
        spec.setQueries(queries);
        spec.setDefaultNamespace(defaultNamespace);
        spec.setDefaultCatalog(defaultCatalog);
        spec.setProperties(properties.isEmpty() ? null : properties);

        return V1IcebergView.builder()
            .withMetadata(ObjectMeta.builder()
                .withName(resourceName)
                .withAnnotation(IcebergAnnotations.VIEW_LOCATION, view.location())
                .build())
            .withSpec(spec)
            .build();
    }

    @NotNull
    private static V1IcebergSchema convertSchema(@NotNull final Schema schema) {
        List<V1IcebergColumn> columns = schema.columns().stream()
            .map(IcebergViewConverter::convertColumn)
            .collect(Collectors.toList());

        V1IcebergSchema result = new V1IcebergSchema();
        result.setColumns(columns);
        return result;
    }

    @NotNull
    private static V1IcebergColumn convertColumn(@NotNull final Types.NestedField field) {
        return new V1IcebergColumn(
            field.name(),
            IcebergTypeMapper.fromIcebergType(field.type()),
            field.isRequired(),
            field.doc(),
            field.initialDefault(),
            field.writeDefault(),
            null  // previousName is only declared in specs, not read from a live view
        );
    }
}
