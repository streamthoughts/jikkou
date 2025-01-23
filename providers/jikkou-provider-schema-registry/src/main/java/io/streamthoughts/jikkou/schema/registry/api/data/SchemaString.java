/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

@Reflectable
public record SchemaString(
    @JsonProperty("schema") String schema
) {

    /**
     * {@inheritDoc}
     **/
    @Override
    public String
    toString() {
        return "{schema=" + schema + '}';
    }
}
