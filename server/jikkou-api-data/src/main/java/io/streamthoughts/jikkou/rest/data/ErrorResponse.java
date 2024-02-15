/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Represents an error response.
 *
 * @param message The top-level error message.
 * @param errors  One or more errors.
 */
@JsonPropertyOrder({
        "message",
        "errors"
})
@Reflectable
public record ErrorResponse(@JsonProperty("message") String message,
                            @JsonProperty("errors") List<ErrorEntity> errors) {

    @ConstructorProperties({
            "message",
            "errors"
    })
    public ErrorResponse {}
}
