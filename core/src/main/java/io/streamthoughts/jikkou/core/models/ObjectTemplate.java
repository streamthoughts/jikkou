/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "values",
})
@Reflectable
public final class ObjectTemplate {

    private final Map<String, Object> values;

    public ObjectTemplate() {
        this(null);
    }

    /**
     * Creates a new {@link ObjectTemplate} instance.
     *
     * @param values    the values to be passed to the template engine.
     */
    @ConstructorProperties({
            "values",
    })
    public ObjectTemplate(final Map<String, Object> values) {
        this.values = values;
    }

    @JsonProperty("values")
    public Map<String, Object> getValues() {
        return values;
    }

    public Optional<Map<String, Object>> optionalValues() {
        return Optional.ofNullable(values);
    }
}
