/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;

@JsonPropertyOrder({
        "op",
        "data",
        "changes"
})
@Reflectable
@JsonDeserialize(as = GenericResourceChangeSpec.class)
public interface ResourceChangeSpec extends Change {

    /**
     * Gets the operation type associated to this change.
     *
     * @return The change type.
     */
    @JsonProperty("op")
    Operation getOp();

    /**
     * Gets the custom data associated to this change. This method return an opaque data.
     * Classes implementing this interface may override this method to return a more specific type.
     *
     * @return  The data object, or {@code null} if the specification has no custom data.
     */
    @JsonProperty("data")
    default Object getData() {
        return null;
    }

    /**
     * Gets the state changes.
     *
     * @return The list of state changes.
     */
    @JsonProperty("changes")
    StateChangeList<? extends StateChange> getChanges();

    /**
     * Sets the changes.
     *
     * @param changes   The changes.
     */
    void setChanges(StateChangeList<StateChange> changes);

    /**
     * Creates a new {@link ResourceChangeSpecBuilder} instance.
     *
     * @return The new {@link ResourceChangeSpecBuilder}.
     */
    static ResourceChangeSpecBuilder builder() {
        return new ResourceChangeSpecBuilder();
    }
}
