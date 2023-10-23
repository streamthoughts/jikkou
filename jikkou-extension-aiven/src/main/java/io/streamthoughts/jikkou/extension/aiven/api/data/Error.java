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

/**
 * Represents an error occurred during request processing.
 *
 * @param message   Printable error message
 * @param moreInfo  URL to the documentation of the error.
 * @param status    HTTP error status code
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
