/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;


import java.util.Locale;

/**
 * Kafka ACL permissions for Topics / Schema Registry
 */
public enum Permission {

    ADMIN,
    READ,
    READWRITE,
    WRITE;

    /**
     * Return a string representation of this enum.
     *
     * @return the lowercase string.
     */
    public String val() {
        return super.name().toLowerCase(Locale.ROOT);
    }
}
