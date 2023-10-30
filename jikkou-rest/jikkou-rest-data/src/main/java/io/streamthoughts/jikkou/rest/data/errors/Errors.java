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
package io.streamthoughts.jikkou.rest.data.errors;

/**
 * Lists of errors that can be returned from the REST Proxy.
 */
public final class Errors {

    public static final String INTERNAL_SERVER_ERROR_CODE = "internal_server_error";
    public static final String API_RESOURCE_TYPE_NOT_FOUND_ERROR_CODE = "apis_resource_type_not_found";
    public static final String API_RESOURCE_VALIDATION_FAILED_ERROR_CODE = "apis_resource_validation_failed";
    public static final String API_HEALTH_INDICATOR_NOT_FOUND = "apis_health_indicator_not_found";
    public static final String AUTHENTICATION_USER_UNAUTHORIZED = "authentication_user_unauthorized";
    public static final String NOT_FOUND = "not_found";

    private Errors() {}
}
