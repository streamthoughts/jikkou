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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.List;

/**
 * Represents an error response from the Aiven API.
 * See <a href="https://api.aiven.io/doc/#section/Errors">https://api.aiven.io/doc/#section/Errors</a>
 *
 * @param message the error message.
 * @param errors  the errors.
 */
@Reflectable
public record ErrorsResponse(String message, List<Error> errors) {

    @JsonCreator
    public ErrorsResponse(@JsonProperty("message") final String message,
                          @JsonProperty("errors") final List<Error> errors) {
        this.message = message;
        this.errors = errors;
    }

    /**
     * Represents a single error.
     *
     * @param message   Printable error message
     * @param status    HTTP error status code
     * @param errorCode Machine-readable error_code
     */
    public record Error(String message, int status, String errorCode) {

        @JsonCreator
        public Error(@JsonProperty("message") final String message,
                     @JsonProperty("status") final int status,
                     @JsonProperty("error_code") final String errorCode) {
            this.message = message;
            this.status = status;
            this.errorCode = errorCode;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public String toString() {
            return "{" +
                    "\"message\":\"" + message + "\"" +
                    ", \"status\": " + status +
                    ", \"errorCode\": \" " + errorCode
                    + "\"}";
        }
    }

}
