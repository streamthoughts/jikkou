/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Provides human-readable descriptions for namespace changes.
 */
public final class NamespaceChangeDescription implements TextDescription {

    private static final String PROPERTY_PREFIX = "property.";

    private final ResourceChange change;

    /**
     * Creates a new {@link NamespaceChangeDescription} instance.
     *
     * @param change the resource change.
     */
    public NamespaceChangeDescription(@NotNull final ResourceChange change) {
        this.change = change;
    }

    /** {@inheritDoc} */
    @Override
    public String textual() {
        String name = change.getMetadata().getName();
        Operation op = change.getSpec().getOp();

        return switch (op) {
            case CREATE -> "Create namespace '" + name + "'";
            case DELETE -> "Delete namespace '" + name + "'";
            case UPDATE -> {
                List<String> keys = change.getSpec().getChanges().stream()
                    .filter(c -> c.getOp() != Operation.NONE)
                    .filter(c -> c.getName().startsWith(PROPERTY_PREFIX))
                    .map(StateChange::getName)
                    .map(k -> k.substring(PROPERTY_PREFIX.length()))
                    .collect(Collectors.toList());
                String setList = keys.isEmpty() ? "" : " (set=[" + String.join(", ", keys) + "])";
                yield "Update namespace '" + name + "'" + setList;
            }
            default -> op + " namespace '" + name + "'";
        };
    }
}
