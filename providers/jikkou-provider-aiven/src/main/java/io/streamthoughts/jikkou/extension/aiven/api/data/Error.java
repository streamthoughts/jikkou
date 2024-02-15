/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Represents an error occurred during request processing.
 *
 * @param message   Printable error message
 * @param moreInfo  URL to the documentation of the error.
 * @param status    HTTP error status name
 * @param errorCode Machine-readable error_code
 */
@Reflectable
public record Error(String message, String moreInfo, int status, String errorCode) {

    @ConstructorProperties({
            "message",
            "more_info",
            "status",
            "error_code"
    })
    public Error {}
}
