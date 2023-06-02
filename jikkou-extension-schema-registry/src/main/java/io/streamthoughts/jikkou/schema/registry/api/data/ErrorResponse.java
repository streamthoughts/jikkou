/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class ErrorResponse {

    private final int errorCode;
    private final String message;

    /**
     * Creates a new {@link ErrorResponse} instance.
     *
     * @param errorCode the error code.
     * @param message   the error message.
     */
    public ErrorResponse(@JsonProperty("error_code") int errorCode,
                         @JsonProperty("message") String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int errorCode() {
        return errorCode;
    }

    public String message() {
        return message;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return errorCode == that.errorCode && Objects.equals(message, that.message);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "{" +
                "errorCode=" + errorCode +
                ", message=" + message +
                '}';
    }
}
