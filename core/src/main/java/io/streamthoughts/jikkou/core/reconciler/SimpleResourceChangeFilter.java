/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple implementation of the {@link ResourceChangeFilter}.
 */
public final class SimpleResourceChangeFilter implements ResourceChangeFilter {

    public static final String FILTER_RESOURCE_OPS_NAME = "filter-resource-op";
    public static final String FILTER_CHANGE_OP_NAME = "filter-change-op";
    private Set<Operation> filterOutAllResourcesExcept = Collections.emptySet();
    private Set<Operation> filterOutAllChangesExcept = Collections.emptySet();

    /**
     * Creates a new {@link SimpleResourceChangeFilter} instance.
     */
    public SimpleResourceChangeFilter() {
    }

    /**
     * Sets the operations which resources should be kept.
     *
     * @param operations The operations.
     * @return {@code this}
     */
    public SimpleResourceChangeFilter filterOutAllResourcesExcept(Set<Operation> operations) {
        this.filterOutAllResourcesExcept = operations;
        return this;
    }

    /**
     * Sets the operations which changes should be kept.
     *
     * @param operations The operations.
     * @return {@code this}
     */
    public SimpleResourceChangeFilter filterOutAllChangesExcept(Set<Operation> operations) {
        this.filterOutAllChangesExcept = operations;
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> filter(List<ResourceChange> changes) {
        return changes.stream()
                .filter(change -> ChangeFilter.filterOutAllExcept(filterOutAllResourcesExcept).test(change))
                .map(change -> {
                    if (!filterOutAllChangesExcept.isEmpty()) {
                        // apply filter on states
                        ChangeFilter<Change> filter = ChangeFilter.filterOutAllExcept(filterOutAllChangesExcept);
                        List<StateChange> filtered = change.getSpec()
                                .getChanges()
                                .all()
                                .stream()
                                .filter(filter)
                                .collect(Collectors.toList());
                        change.getSpec().setChanges(StateChangeList.of(filtered));
                    }
                    // build a new resource change.
                    return change;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public NamedValueSet toValues() {
        return NamedValueSet.emptySet()
                .with(FILTER_RESOURCE_OPS_NAME, filterOutAllResourcesExcept.stream().map(Enum::name).collect(Collectors.joining(",")))
                .with(FILTER_CHANGE_OP_NAME, filterOutAllChangesExcept.stream().map(Enum::name).collect(Collectors.joining(",")));
    }
}
