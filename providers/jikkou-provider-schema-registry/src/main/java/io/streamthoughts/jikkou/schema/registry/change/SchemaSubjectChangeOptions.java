/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Represents options that can be used to apply a change.
 */
@Reflectable
public record SchemaSubjectChangeOptions(
        @JsonProperty("permanentDelete") boolean permanentDelete,
        @JsonProperty("normalizeSchema") boolean normalizeSchema
) {

    @ConstructorProperties({
            "permanentDelete",
            "normalizeSchema"
    })
    public SchemaSubjectChangeOptions {

    }
}
