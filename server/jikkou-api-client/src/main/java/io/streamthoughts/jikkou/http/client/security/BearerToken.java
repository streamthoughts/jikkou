/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.security;

/**
 * Authentication token.
 *
 * @param value The token value.
 */
public record BearerToken(String value) {

    public static BearerToken empty() {
        return new BearerToken(null);
    }
}
