/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Provides human-readable descriptions for Iceberg table changes.
 */
public final class TableChangeDescription implements TextDescription {

    private static final String COLUMN_PREFIX = "column.";
    private static final String RENAME_SUFFIX = ".rename";
    private static final String PROPERTY_PREFIX = "property.";

    private final ResourceChange change;

    /**
     * Creates a new {@link TableChangeDescription} instance.
     *
     * @param change the resource change.
     */
    public TableChangeDescription(@NotNull final ResourceChange change) {
        this.change = change;
    }

    /** {@inheritDoc} */
    @Override
    public String textual() {
        String name = change.getMetadata().getName();
        Operation op = change.getSpec().getOp();

        return switch (op) {
            case CREATE -> buildCreateDescription(name);
            case DELETE -> "Delete table '" + name + "'";
            case UPDATE -> buildUpdateDescription(name);
            default -> op + " table '" + name + "'";
        };
    }

    private String buildCreateDescription(@NotNull final String name) {
        var changes = change.getSpec().getChanges();

        // Count columns from schema.columns state change
        int columnCount = 0;
        List<String> partitionTransforms = List.of();
        String format = null;

        for (StateChange sc : changes) {
            if ("schema.columns".equals(sc.getName()) && sc.getAfter() instanceof List<?> cols) {
                columnCount = cols.size();
            }
            if ("partitionSpec".equals(sc.getName()) && sc.getAfter() instanceof List<?> parts) {
                partitionTransforms = parts.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            }
            if ("property.write.format.default".equals(sc.getName()) && sc.getAfter() != null) {
                format = String.valueOf(sc.getAfter());
            }
        }

        StringBuilder sb = new StringBuilder("Create table '").append(name).append("'");
        sb.append(" (columns=").append(columnCount);
        if (!partitionTransforms.isEmpty()) {
            sb.append(", partitions=[").append(String.join(", ", partitionTransforms)).append("]");
        }
        if (format != null) {
            sb.append(", format=").append(format);
        }
        sb.append(")");
        return sb.toString();
    }

    private String buildUpdateDescription(@NotNull final String name) {
        var changes = change.getSpec().getChanges();

        List<String> renames = changes.stream()
            .filter(c -> c.getName().startsWith(COLUMN_PREFIX) && c.getName().endsWith(RENAME_SUFFIX))
            .filter(c -> c.getOp() != Operation.NONE)
            .map(c -> {
                Object before = c.getBefore();
                Object after = c.getAfter();
                return before + "\u2192" + after;
            })
            .collect(Collectors.toList());

        List<String> adds = changes.stream()
            .filter(c -> c.getName().startsWith(COLUMN_PREFIX))
            .filter(c -> !c.getName().endsWith(RENAME_SUFFIX))
            .filter(c -> c.getOp() == Operation.CREATE)
            .map(c -> c.getName().substring(COLUMN_PREFIX.length()))
            .collect(Collectors.toList());

        List<String> drops = changes.stream()
            .filter(c -> c.getName().startsWith(COLUMN_PREFIX))
            .filter(c -> !c.getName().endsWith(RENAME_SUFFIX))
            .filter(c -> c.getOp() == Operation.DELETE)
            .map(c -> c.getName().substring(COLUMN_PREFIX.length()))
            .collect(Collectors.toList());

        List<String> properties = changes.stream()
            .filter(c -> c.getName().startsWith(PROPERTY_PREFIX))
            .filter(c -> c.getOp() != Operation.NONE)
            .map(c -> c.getName().substring(PROPERTY_PREFIX.length()))
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Update table '").append(name).append("'");
        boolean hasDetails = !renames.isEmpty() || !adds.isEmpty() || !drops.isEmpty() || !properties.isEmpty();
        if (hasDetails) {
            sb.append(" (");
            boolean first = true;
            if (!renames.isEmpty()) {
                sb.append("rename=[").append(String.join(", ", renames)).append("]");
                first = false;
            }
            if (!adds.isEmpty()) {
                if (!first) sb.append(", ");
                sb.append("add=[").append(String.join(", ", adds)).append("]");
                first = false;
            }
            if (!drops.isEmpty()) {
                if (!first) sb.append(", ");
                sb.append("drop=[").append(String.join(", ", drops)).append("]");
                first = false;
            }
            if (!properties.isEmpty()) {
                if (!first) sb.append(", ");
                sb.append("properties=[").append(String.join(", ", properties)).append("]");
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
