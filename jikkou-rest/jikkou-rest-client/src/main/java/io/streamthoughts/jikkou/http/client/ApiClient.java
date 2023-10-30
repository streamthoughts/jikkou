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
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiResponseException;
import io.streamthoughts.jikkou.http.client.serdes.JSON;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public final class ApiClient {

    private String basePath;
    private Map<String, String> defaultHeaderMap = new HashMap<>();
    private Map<String, String> defaultCookieMap = new HashMap<>();
    private final OkHttpClient httpClient;

    private JSON json = null;

    /**
     * Basic constructor for ApiClient
     */
    public ApiClient(@NotNull OkHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    /**
     * Sets the default headers.
     *
     * @param defaultHeaderMap the default header map.
     * @return ApiClient
     */
    public ApiClient setDefaultHeaders(Map<String, String> defaultHeaderMap) {
        this.defaultHeaderMap = new HashMap<>(defaultHeaderMap);
        return this;
    }

    /**
     * Add a default header.
     *
     * @param key   The header's key
     * @param value The header's value
     * @return ApiClient
     */
    public ApiClient addDefaultHeader(String key, String value) {
        defaultHeaderMap.put(key, value);
        return this;
    }

    /**
     * Sets the default cookies.
     *
     * @param defaultCookieMap the default cookie map.
     * @return ApiClient
     */
    public ApiClient setDefaultCookies(final Map<String, String> defaultCookieMap) {
        this.defaultCookieMap = defaultCookieMap;
        return this;
    }

    /**
     * Add a default cookie.
     *
     * @param key   The cookie's key
     * @param value The cookie's value
     * @return ApiClient
     */
    public ApiClient addDefaultCookie(String key, String value) {
        defaultCookieMap.put(key, value);
        return this;
    }

    /**
     * Get base path
     *
     * @return Base path
     */
    public String getBasePath() {
        return basePath;
    }


    /**
     * Get base path URI
     *
     * @return Base path
     */
    public HttpUrl getBaseUrl() {
        return HttpUrl.get(basePath);
    }

    /**
     * Set base path
     *
     * @param basePath Base path of the URL (e.g. <a href="http://localhost">...</a>)
     * @return An instance of OkHttpClient
     */
    public ApiClient setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    /**
     * Set JSON
     *
     * @param json JSON object
     * @return Api client
     */
    public ApiClient setJSON(JSON json) {
        this.json = json;
        return this;
    }

    /**
     * {@link #execute(Call, Class)}
     *
     * @param <T> Class
     * @return ApiResponse
     * @throws JikkouApiClientException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Request request) throws JikkouApiClientException {
        return execute(httpClient.newCall(request));
    }

    /**
     * {@link #execute(Call, Class)}
     *
     * @param <T>  Class
     * @param call An instance of the Call object
     * @return ApiResponse
     * @throws JikkouApiClientException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Call call) throws JikkouApiClientException {
        return execute(call, null);
    }

    /**
     * Execute HTTP call and deserialize the HTTP response body into the given return type.
     *
     * @param returnType The return type used to deserialize HTTP response body
     * @param <T>        The return type corresponding to (same with) returnType
     * @return ApiResponse object containing response status, headers and data, which is a Java object
     * deserialized from response body and would be null when returnType is null.
     * @throws JikkouApiClientException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Request request, Class<T> returnType) throws JikkouApiClientException {
        return execute(httpClient.newCall(request), returnType);
    }

    /**
     * Execute HTTP call and deserialize the HTTP response body into the given return type.
     *
     * @param returnType The return type used to deserialize HTTP response body
     * @param <T>        The return type corresponding to (same with) returnType
     * @param call       Call
     * @return ApiResponse object containing response status, headers and data, which is a Java object
     * deserialized from response body and would be null when returnType is null.
     * @throws JikkouApiClientException If fail to execute the call
     */
    public <T> ApiResponse<T> execute(Call call, Class<T> returnType) throws JikkouApiClientException {
        try {
            Response response = call.execute();
            T data = handleResponse(response, returnType);
            return new ApiResponse<>(response.code(), response.headers().toMultimap(), data);
        } catch (IOException e) {
            throw new JikkouApiClientException(e);
        }
    }

    /**
     * {@link #executeAsync(Call, Class, ApiCallback)}
     *
     * @param <T>      Class
     * @param call     An instance of the Call object
     * @param callback ApiCallback
     */
    public <T> void executeAsync(Call call, ApiCallback<T> callback) {
        executeAsync(call, null, callback);
    }

    /**
     * Execute HTTP call asynchronously.
     *
     * @param <T>        Class
     * @param call       The callback to be executed when the API call finishes
     * @param returnType Return type
     * @param callback   ApiCallback
     * @see #execute(Call, Class)
     */
    public <T> void executeAsync(Call call, final Class<T> returnType, final ApiCallback<T> callback) {
        call.enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onFailure(new JikkouApiResponseException(e), 0, null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        T result;
                        try {
                            result = handleResponse(response, returnType);
                        } catch (JikkouApiResponseException e) {
                            callback.onFailure(e, response.code(), response.headers().toMultimap());
                            return;
                        } catch (Exception e) {
                            callback.onFailure(
                                    new JikkouApiResponseException(e), response.code(), response.headers().toMultimap());
                            return;
                        }
                        callback.onSuccess(result, response.code(), response.headers().toMultimap());
                    }
                });
    }


    /**
     * Set header parameters to the request builder, including default headers.
     *
     * @param headerParams Header parameters in the form of Map
     * @param reqBuilder   Request.Builder
     */
    private void processHeaderParams(Map<String, String> headerParams, Request.Builder reqBuilder) {
        for (Map.Entry<String, String> param : headerParams.entrySet()) {
            reqBuilder.header(param.getKey(), parameterToString(param.getValue()));
        }
        for (Map.Entry<String, String> header : defaultHeaderMap.entrySet()) {
            if (!headerParams.containsKey(header.getKey())) {
                reqBuilder.header(header.getKey(), parameterToString(header.getValue()));
            }
        }
    }

    /**
     * Set cookie parameters to the request builder, including default cookies.
     *
     * @param cookieParams Cookie parameters in the form of Map
     * @param reqBuilder   Request.Builder
     */
    private void processCookieParams(Map<String, String> cookieParams, Request.Builder reqBuilder) {
        for (Map.Entry<String, String> param : cookieParams.entrySet()) {
            reqBuilder.addHeader("Cookie", String.format("%s=%s", param.getKey(), param.getValue()));
        }
        for (Map.Entry<String, String> param : defaultCookieMap.entrySet()) {
            if (!cookieParams.containsKey(param.getKey())) {
                reqBuilder.addHeader("Cookie", String.format("%s=%s", param.getKey(), param.getValue()));
            }
        }
    }

    /**
     * Handle the given response, return the deserialized object when the response is successful.
     *
     * @param <T>        the response type.
     * @param response   Response
     * @param returnType Return type
     * @return the response of type {@code T}.
     * @throws JikkouApiResponseException If the response has an unsuccessful status name or fail to deserialize the
     *                                    response body
     */
    private <T> T handleResponse(Response response, Class<T> returnType) throws JikkouApiResponseException {
        if (response.isSuccessful()) {
            if (returnType == null || response.code() == 204) {
                // returning null if the returnType is not defined,
                // or the status name is 204 (No Content)
                if (response.body() != null) {
                    try {
                        response.body().close();
                    } catch (Exception e) {
                        throw new JikkouApiResponseException(
                                response.message(),
                                e,
                                response.code(),
                                response.headers().toMultimap());
                    }
                }
                return null;
            } else {
                return deserialize(response, returnType);
            }
        } else {
            ErrorResponse errorResponse = null;
            if (response.body() != null) {
                try {
                    String respBody = response.body().string();
                    errorResponse = json.deserialize(respBody, ErrorResponse.class);
                } catch (IOException e) {
                    throw new JikkouApiResponseException(
                            response.message(),
                            e,
                            response.code(),
                            response.headers().toMultimap());
                }
            }
            throw new JikkouApiResponseException(
                    response.message(),
                    response.code(),
                    response.headers().toMultimap(),
                    errorResponse
            );
        }
    }

    /**
     * Deserialize response body to Java object, according to the return type and the Content-Type
     * response header.
     *
     * @param <T>        Type
     * @param response   HTTP response
     * @param returnType The type of the Java object
     * @return The deserialized Java object
     * @throws JikkouApiClientException If fail to deserialize response body, i.e. cannot read response body or
     *                                  the Content-Type of the response is not supported.
     */
    @SuppressWarnings("unchecked")
    private <T> T deserialize(Response response, Class<T> returnType) throws JikkouApiClientException {
        if (response == null || returnType == null) {
            return null;
        }

        String respBody;
        try {
            if (response.body() != null) respBody = response.body().string();
            else respBody = null;
        } catch (IOException e) {
            throw new JikkouApiClientException(e);
        }

        if (respBody == null || respBody.isEmpty()) {
            return null;
        }

        String contentType = response.headers().get("Content-Type");
        if (contentType == null) {
            // ensuring a default content type
            contentType = "application/json";
        }
        if (isJsonMime(contentType)) {
            return json.deserialize(respBody, returnType);
        } else if (returnType.equals(String.class)) {
            // Expecting string, return the raw response body.
            return (T) respBody;
        } else {
            throw new JikkouApiResponseException("Content type \"" + contentType + "\" is not supported for type: " + returnType);
        }
    }

    /**
     * Serialize the given Java object into request body according to the object's class and the
     * request Content-Type.
     *
     * @param obj         The Java object
     * @param contentType The request Content-Type
     * @return The serialized request body
     * @throws JikkouApiClientException If fail to serialize the given object
     */
    public RequestBody serialize(Object obj, String contentType) throws JikkouApiClientException {
        if (isJsonMime(contentType)) {
            String content;
            if (obj != null) {
                content = json.serialize(obj);
            } else {
                content = null;
            }
            return RequestBody.create(content, MediaType.parse(contentType));
        } else {
            throw new JikkouApiClientException("Content type \"" + contentType + "\" is not supported");
        }
    }

    /**
     * Check if the given MIME is a JSON MIME. JSON MIME examples: application/json application/json;
     * charset=UTF8 APPLICATION/JSON application/vnd.company+json "* / *" is also default to JSON
     *
     * @param mime MIME (Multipurpose Internet Mail Extensions)
     * @return True if the given MIME is JSON, false otherwise.
     */
    private boolean isJsonMime(String mime) {
        String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
        return mime != null && (mime.matches(jsonMime) || mime.equals("*/*"));
    }

    public ApiClientBuilder toBuilder() {
        OkHttpClient newClient = httpClient.newBuilder().build();
        ApiClientBuilder builder = new ApiClientBuilder(newClient, defaultHeaderMap, defaultCookieMap);
        return builder.withBasePath(basePath);
    }


    /**
     * Format the given parameter object into string.
     *
     * @param param Parameter
     * @return String representation of the parameter
     */
    public String parameterToString(Object param) {
        if (param == null) {
            return "";
        } else if (param instanceof Date
                || param instanceof OffsetDateTime
                || param instanceof LocalDate) {
            // Serialize to json string and remove the " enclosing characters
            String jsonStr = json.serialize(param);
            return jsonStr.substring(1, jsonStr.length() - 1);
        } else if (param instanceof Collection collection) {
            StringBuilder b = new StringBuilder();
            for (Object o : collection) {
                if (!b.isEmpty()) {
                    b.append(",");
                }
                b.append(o);
            }
            return b.toString();
        } else {
            return String.valueOf(param);
        }
    }
}
