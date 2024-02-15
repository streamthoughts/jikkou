/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Schema Registry - Error Response
 *
 * @param errorCode the error name.
 * @param message   the error message.
 */
@Reflectable
public record ErrorResponse(int errorCode, String message) {

    /**
     * Creates a new {@link ErrorResponse} instance.
     */
    @ConstructorProperties({
            "error_code",
            "message"
    })
    public ErrorResponse { }

}
