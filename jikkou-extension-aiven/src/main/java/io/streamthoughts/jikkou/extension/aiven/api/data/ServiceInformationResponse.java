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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Reflectable
public final class ServiceInformationResponse {

    /**
     * The service information
     */
    private final Map<String, Object> service;

    /**
     * List of errors occurred during request processing
     */
    private final List<Error> errors;

    /**
     * Printable result of the request
     */
    private final String message;

    @JsonCreator
    public ServiceInformationResponse(@JsonProperty("service") Map<String, Object> service,
                                      @JsonProperty("errors") List<Error> errors,
                                      @JsonProperty("message") String message) {
        this.service = service;
        this.errors = errors;
        this.message = message;
    }

    public Map<String, Object> service() {
        return service;
    }

    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }

    public String message() {
        return message;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInformationResponse that = (ServiceInformationResponse) o;
        return Objects.equals(service, that.service) && Objects.equals(errors, that.errors) && Objects.equals(message, that.message);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(service, errors, message);
    }
    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "ServiceInformation{" +
                "service=" + service +
                ", errors=" + errors +
                ", message='" + message + '\'' +
                '}';
    }
}
