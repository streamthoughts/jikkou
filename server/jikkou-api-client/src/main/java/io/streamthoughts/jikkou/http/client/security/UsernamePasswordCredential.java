/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.security;

import org.jetbrains.annotations.Nullable;

public record UsernamePasswordCredential(@Nullable String username, @Nullable String password) {

    public static UsernamePasswordCredential empty() {
        return new UsernamePasswordCredential(null, null);
    }

}
