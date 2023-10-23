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
package io.streamthoughts.jikkou.rest.client;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Top-level exception for the RestClient.
 */
public class RestClientException extends JikkouRuntimeException {

    private final WebApplicationException error;
    private final String requestMethod;
    private final String requestUrl;

    public RestClientException(WebApplicationException error,
                               String requestUrl,
                               String requestMethod) {
        super(error);
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.error = error;
    }

    /** {@inheritDoc} **/
    @Override
    public String getLocalizedMessage() {
        return error.getLocalizedMessage();
    }

    /** {@inheritDoc} **/
    @Override
    public String getMessage() {
        return error.getMessage();
    }

    /**
     * Get the executed url.
     *
     * @return the string request url.
     */
    public String requestUrl() {
        return requestUrl;
    }

    /**
     * Get the http method.
     *
     * @return the string method
     */
    public String requestMethod() {
        return requestMethod;
    }

    /**
     * Get the HTTP response.
     *
     * @return the HTTP response.
     */
    public Response response() {
        return error.getResponse();
    }

    /**
     * Get the response entity.
     *
     * @return the string response entity, or {@code null}.
     */
    public String getResponseEntity() {
        return getResponseEntity(String.class);
    }

    /**
     * Get the response entity.
     * @param entityType the entity type.
     *
     * @return the response entity, or {@code null}.
     */
    public <T> T getResponseEntity(Class<T> entityType) {
        Response response = error.getResponse();
        if (response.hasEntity()) {
            return response.readEntity(entityType);
        }
        return null;
    }
}
