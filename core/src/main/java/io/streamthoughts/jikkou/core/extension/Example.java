/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Represents an extension example.
 *
 * @param title The title of the example.
 * @param code  The code example.
 */
@Reflectable
@JsonPropertyOrder({
        "title",
        "code"
})
public record Example(@JsonProperty("title") String title, @JsonProperty("code") String[] code) {

    @ConstructorProperties({
            "title",
            "code"
    })
    public Example {

    }
}
