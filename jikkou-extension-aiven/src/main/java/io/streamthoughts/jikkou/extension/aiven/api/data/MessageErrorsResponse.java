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
