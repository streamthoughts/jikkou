/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg;

/**
 * Constants for API versions used by the Iceberg provider.
 */
public final class ApiVersions {

    /** The current API version for Iceberg resources. */
    public static final String ICEBERG_V1BETA1 = "iceberg.jikkou.io/v1beta1";

    private ApiVersions() {
        // utility class
    }
}
