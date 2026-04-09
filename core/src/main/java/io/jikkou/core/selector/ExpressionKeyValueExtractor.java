/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.selector;

import io.jikkou.core.models.HasMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExpressionKeyValueExtractor {

    String getKeyValue(final @NotNull HasMetadata resource,
                       final @Nullable String key);

    boolean isKeyExists(final @NotNull HasMetadata resource,
                        final @Nullable String key);


}
