/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import org.jetbrains.annotations.NotNull;

public interface MatchExpression {

    /**
     * Apply this matching selector expression on the given resource.
     *
     * @param resource the resource to be matched.
     * @return         {@code true} if the resource match, otherwise {@code false}.
     */
    boolean apply(@NotNull HasMetadata resource);
}
