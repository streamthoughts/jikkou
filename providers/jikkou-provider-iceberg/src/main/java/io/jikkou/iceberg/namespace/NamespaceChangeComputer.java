/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.change.ChangeComputer;
import io.jikkou.core.reconciler.change.ChangeComputerBuilder;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Computes changes for Iceberg namespace resources.
 */
public final class NamespaceChangeComputer extends ResourceChangeComputer<String, V1IcebergNamespace> {

    /**
     * Creates a new {@link NamespaceChangeComputer} instance.
     *
     * @param deleteOrphans whether to delete namespaces absent from the desired state.
     */
    public NamespaceChangeComputer(final boolean deleteOrphans) {
        super(
            ChangeComputerBuilder.KeyMapper.byName(),
            new NamespaceChangeFactory(),
            deleteOrphans
        );
    }

    /**
     * Factory that produces {@link ResourceChange} objects for namespace changes.
     */
    public static final class NamespaceChangeFactory extends ResourceChangeFactory<String, V1IcebergNamespace> {

        private static final String PROPERTY_PREFIX = "property.";

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForCreate(@NotNull final String key,
                                                    @NotNull final V1IcebergNamespace after) {
            List<StateChange> changes = new ArrayList<>();
            getProperties(after).forEach((k, v) ->
                changes.add(StateChange.create(PROPERTY_PREFIX + k, v)));
            return buildResourceChange(after, Operation.CREATE, changes);
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForDelete(@NotNull final String key,
                                                    @NotNull final V1IcebergNamespace before) {
            List<StateChange> changes = new ArrayList<>();
            getProperties(before).forEach((k, v) ->
                changes.add(StateChange.delete(PROPERTY_PREFIX + k, v)));
            return buildResourceChange(before, Operation.DELETE, changes);
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForUpdate(@NotNull final String key,
                                                    @NotNull final V1IcebergNamespace before,
                                                    @NotNull final V1IcebergNamespace after) {
            Map<String, String> beforeProps = getProperties(before);
            Map<String, String> afterProps = getProperties(after);

            ChangeComputer<Map.Entry<String, String>, StateChange> propComputer = ChangeComputer
                .<String, Map.Entry<String, String>, StateChange>builder()
                .withKeyMapper(Map.Entry::getKey)
                .withChangeFactory((propKey, beforeEntry, afterEntry) -> {
                    String bVal = beforeEntry != null ? beforeEntry.getValue() : null;
                    String aVal = afterEntry != null ? afterEntry.getValue() : null;
                    return Optional.of(StateChange.with(PROPERTY_PREFIX + propKey, bVal, aVal));
                })
                .withDeleteOrphans(false)
                .build();

            List<StateChange> changes = propComputer.computeChanges(
                new ArrayList<>(beforeProps.entrySet()),
                new ArrayList<>(afterProps.entrySet())
            );

            boolean hasChanged = changes.stream().anyMatch(c -> c.getOp() != Operation.NONE);
            Operation op = hasChanged ? Operation.UPDATE : Operation.NONE;

            return buildResourceChange(before, op, changes);
        }

        @NotNull
        private static Map<String, String> getProperties(@Nullable final V1IcebergNamespace resource) {
            if (resource == null || resource.getSpec() == null || resource.getSpec().getProperties() == null) {
                return Collections.emptyMap();
            }
            return resource.getSpec().getProperties();
        }

        @NotNull
        private static ResourceChange buildResourceChange(@NotNull final V1IcebergNamespace resource,
                                                          @NotNull final Operation op,
                                                          @NotNull final List<StateChange> changes) {
            return GenericResourceChange.builder(V1IcebergNamespace.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec.builder()
                    .withOperation(op)
                    .withChanges(changes)
                    .build())
                .build();
        }
    }
}
