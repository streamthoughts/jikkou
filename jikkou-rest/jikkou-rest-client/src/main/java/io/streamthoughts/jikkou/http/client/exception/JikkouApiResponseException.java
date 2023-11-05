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
package io.streamthoughts.jikkou.http.client.exception;

import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import java.util.List;
import java.util.Map;

public class JikkouApiResponseException extends JikkouApiClientException {
    private int code = 0;
    private Map<String, List<String>> responseHeaders = null;
    private ErrorResponse errorResponse = null;

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     */
    public JikkouApiResponseException() {
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param throwable       the exception.
     */
    public JikkouApiResponseException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param message         the error message.
     */
    public JikkouApiResponseException(String message) {
        super(message);
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param message         the error message.
     * @param throwable       the exception.
     * @param code            the HTTP response error name.
     * @param responseHeaders the HTTP response header.
     * @param errorResponse   the HTTP response payload.
     */
    public JikkouApiResponseException(
            String message,
            Throwable throwable,
            int code,
            Map<String, List<String>> responseHeaders,
            ErrorResponse errorResponse) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.errorResponse = errorResponse;
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param message         the error message.
     * @param code            the HTTP response error name.
     * @param responseHeaders the HTTP response header.
     * @param errorResponse   the HTTP response payload.
     */
    public JikkouApiResponseException(String message,
                                      int code,
                                      Map<String, List<String>> responseHeaders,
                                      ErrorResponse errorResponse) {
        this(message, (Throwable) null, code, responseHeaders, errorResponse);
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param message         the error message.
     * @param code            the HTTP response error name.
     * @param responseHeaders the HTTP response header.
     */
    public JikkouApiResponseException(String message,
                                      Throwable throwable,
                                      int code,
                                      Map<String, List<String>> responseHeaders) {
        this(message, throwable, code, responseHeaders, null);
    }

    /**
     * Create a new {@link JikkouApiResponseException} instance.
     *
     * @param code            the HTTP response error name.
     * @param responseHeaders the HTTP response header.
     * @param errorResponse   the HTTP response payload.
     */
    public JikkouApiResponseException(int code,
                                      Map<String, List<String>> responseHeaders,
                                      ErrorResponse errorResponse) {
        this(null, null, code, responseHeaders, errorResponse);
    }

    public JikkouApiResponseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public JikkouApiResponseException(int code,
                                      String message,
                                      Map<String, List<String>> responseHeaders,
                                      ErrorResponse errorResponse) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.errorResponse = errorResponse;
    }

    /**
     * Get the HTTP status name.
     *
     * @return HTTP status name
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the HTTP response headers.
     *
     * @return A map of list of string
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
