/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Represents an error response from the Aiven API.
 * See <a href="https://api.aiven.io/doc/#section/Errors">https://api.aiven.io/doc/#section/Errors</a>
 *
 * @param message the error message.
 * @param errors  the errors.
 */
@Reflectable
public record MessageErrorsResponse(String message, List<Error> errors) {

    @ConstructorProperties({
            "message",
            "errors"
    })
    public MessageErrorsResponse {
    }

    /**
     * Represents a single error.
     *
     * @param message   Printable error message
     * @param status    HTTP error status name
     * @param errorCode Machine-readable error_code
     */
    public record Error(String message, int status, String errorCode) {

        @ConstructorProperties({
                "message",
                "status",
                "error_code"
        })
        public Error {
        }

    }
}
