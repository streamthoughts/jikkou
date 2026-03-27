/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg;

/**
 * Constants for Iceberg annotation keys.
 */
public final class IcebergAnnotations {

    private static final String ICEBERG_JIKKOU_IO = "iceberg.jikkou.io/";

    public static final String NAMESPACE_LOCATION = ICEBERG_JIKKOU_IO + "namespace-location";
    public static final String TABLE_LOCATION = ICEBERG_JIKKOU_IO + "table-location";
    public static final String ALLOW_INCOMPATIBLE_CHANGES = ICEBERG_JIKKOU_IO + "allow-incompatible-changes";

    private IcebergAnnotations() {}
}
