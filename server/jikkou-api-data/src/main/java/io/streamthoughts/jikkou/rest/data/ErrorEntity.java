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
package io.streamthoughts.jikkou.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an error.
 *
 * @param status    HTTP status name.
 * @param errorCode Error name (provides machine-readable information on the error).
 * @param message   Error message (provides human-readable information on the error).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "error_code",
        "message",
        "details"
})
@Reflectable
public record ErrorEntity(@JsonProperty("status") int status,
                          @NotNull @JsonProperty("error_code") String errorCode,
                          @JsonProperty("message") String message,
                          @JsonProperty("details") Map<String, Object> details) {

    @ConstructorProperties({
            "status",
            "error_code",
            "message",
            "details"
    })
    public ErrorEntity {
        Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }


    /**
     * Creates a new {@link ErrorEntity} instance.
     *
     * @param status    HTTP status name.
     * @param errorCode Error name (provides machine-readable information on the error).
     * @param message   Error message (provides human-readable information on the error).
     */
    public ErrorEntity(@NotNull @JsonProperty("status") int status,
                       @NotNull @JsonProperty("error_code") String errorCode,
                       @JsonProperty("message") String message) {
        this(status, errorCode, message, null);
    }

    /**
     * Creates a new {@link ErrorEntity} instance.
     *
     * @param status    HTTP status name.
     * @param errorCode Error name (provides machine-readable information on the error).
     */
    public ErrorEntity(@NotNull @JsonProperty("status") int status,
                       @NotNull @JsonProperty("error_code") String errorCode) {
        this(status, errorCode, null, null);
    }
}
