/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * Schema Registry - Error Response
 *
 * @param errorCode the error name.
 * @param message   the error message.
 */
@Reflectable
public record ErrorResponse(
    @JsonProperty("error_code")
    int errorCode,
    @JsonProperty("message")
    String message) {
}
