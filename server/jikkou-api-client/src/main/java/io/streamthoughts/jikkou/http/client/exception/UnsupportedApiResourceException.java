/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.exception;

import org.jetbrains.annotations.NotNull;

public class UnsupportedApiResourceException extends JikkouApiClientException {

    /**
     * Creates a new {@link UnsupportedApiResourceException} instance.
     */
    public UnsupportedApiResourceException(@NotNull String group,
                                           @NotNull String version,
                                           @NotNull String kind) {
        super(String.format(
                "Resource for group %s, version %s, and kind %s is not supported by the remote server.",
                group,
                version,
                kind
        ));
    }
}
