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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Description("")
@JsonPropertyOrder({
        "operation",
        "properties",
        "changes",
})
@JsonDeserialize
@Reflectable
public final class GenericResourceChangeSpec implements SpecificResourceChangeSpec<Map<String, Object>> {

    private final Operation operation;
    private final Map<String, Object> data;
    private final StateChangeList<StateChange> changes;

    /**
     * Creates a new {@link GenericResourceChangeSpec} instance.
     *
     * @param changes The changes.
     */
    public GenericResourceChangeSpec(@NotNull List<StateChange> changes) {
        this(Change.computeOperation(changes), changes, new LinkedHashMap<>());
    }

    /**
     * Creates a new {@link GenericResourceChangeSpec} instance.
     *
     * @param operation The change operation.
     * @param changes   The list of changes.
     * @param data      The data.
     */
    @ConstructorProperties({
            "op",
            "changes",
            "data",
    })
    public GenericResourceChangeSpec(@NotNull Operation operation,
                                     @NotNull List<StateChange> changes,
                                     @NotNull Map<String, Object> data) {
        this.operation = operation;
        this.data = Collections.unmodifiableMap(data);
        this.changes = StateChangeList.of(changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonProperty("op")
    public Operation getOp() {
        return operation;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonProperty("data")
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonProperty("changes")
    public StateChangeList<StateChange> getChanges() {
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        GenericResourceChangeSpec that = (GenericResourceChangeSpec) object;
        return operation == that.operation &&
                Objects.equals(changes, that.changes) &&
                Objects.equals(data, that.data);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(operation, changes, data);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "GenericResourceChangeSpec[" +
                "operation=" + operation +
                ", changes=" + changes +
                ", data=" + data +
                ']';
    }
}
