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
package io.streamthoughts.jikkou.core.models.change;

import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating new {@link ResourceChangeSpec} instances.
 */
public final class ResourceChangeSpecBuilder {

    private final List<StateChange> changes = new LinkedList<>();
    private NamedValueSet data = NamedValueSet.emptySet();
    private Operation operation;

    /**
     * Creates a new {@link ResourceChangeSpecBuilder} instance.
     */
    ResourceChangeSpecBuilder() {
    }

    /**
     * Add a data property for the given name and value.
     *
     * @param name  The property name.
     * @param value The property value.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withData(String name, Object value) {
        this.data = this.data.with(name, value);
        return this;
    }

    /**
     * Add the given data properties.
     *
     * @param data The data.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withData(Map<String, ?> data) {
        this.data = this.data.with(NamedValueSet.setOf(data));
        return this;
    }

    public Operation operation() {
        return operation;
    }

    /**
     * Sets the change operation.
     *
     * @param operation The operation.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Adds the given state change.
     *
     * @param change The state change.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withChange(StateChange change) {
        this.changes.add(change);
        return this;
    }

    /**
     * Adds the given state changes.
     *
     * @param changes The state changes.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withChanges(List<? extends StateChange> changes) {
        this.changes.addAll(changes);
        return this;
    }

    /**
     * Adds the given data changes.
     *
     * @param changes The daya changes.
     * @return {@code this}.
     */
    public ResourceChangeSpecBuilder withChanges(StateChangeList<StateChange> changes) {
        this.changes.addAll(changes.all());
        return this;
    }

    /**
     * Create the {@link ResourceChangeSpec} instance.
     *
     * @return The new {@link ResourceChangeSpec}.
     */
    public GenericResourceChangeSpec build() {
        return new GenericResourceChangeSpec(
                operation,
                changes,
                data.asMap()
        );
    }
}
