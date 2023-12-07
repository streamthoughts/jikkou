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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.beans.ConstructorProperties;

@JsonPropertyOrder({
        "name",
        "op",
        "before",
        "after"
})
@JsonDeserialize
public class SpecificStateChange<T> extends GenericStateChange {

    /**
     * Creates a new {@link SpecificStateChange} instance.
     *
     * @param name   The name of the data value.
     * @param op     The type of the change.
     * @param before The old data value.
     * @param after  The new data value.
     */
    @ConstructorProperties({
            "name",
            "op",
            "before",
            "after",
            "description"
    })
    public SpecificStateChange(final String name,
                               final Operation op,
                               final T before,
                               final T after) {
        super(name, op, before, after, null);
    }

    /**
     * Creates a new {@link SpecificStateChange} instance.
     *
     * @param name   The name of the data value.
     * @param op     The type of the change.
     * @param before The old data value.
     * @param after  The new data value.
     */
    @ConstructorProperties({
            "name",
            "op",
            "before",
            "after",
            "description"
    })
    public SpecificStateChange(final String name,
                               final Operation op,
                               final T before,
                               final T after,
                               final String description) {
        super(name, op, before, after, description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonProperty("before")
    @SuppressWarnings("unchecked")
    public T getBefore() {
        return (T) super.getBefore();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @JsonProperty("after")
    @SuppressWarnings("unchecked")
    public T getAfter() {
        return (T) super.getAfter();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SpecificStateChange<T> withName(String name) {
        return new SpecificStateChange<>(name, getOp(), getBefore(), getAfter(), getDescription());
    }
}
