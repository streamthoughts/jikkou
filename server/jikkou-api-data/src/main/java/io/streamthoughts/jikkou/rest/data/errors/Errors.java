/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data.errors;

/**
 * Lists of errors that can be returned from the REST API.
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
