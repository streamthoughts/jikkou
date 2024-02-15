/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Reflectable
public record ChangeError(@JsonProperty("message") @NotNull String message,
                          @JsonProperty("status") @Nullable Integer status) {

    public ChangeError(final String message) {
        this(message, null);
    }

    @ConstructorProperties({
            "message",
            "status"
    })
    public ChangeError {
    }


    @Override
    public String toString() {
        return message;
    }
}