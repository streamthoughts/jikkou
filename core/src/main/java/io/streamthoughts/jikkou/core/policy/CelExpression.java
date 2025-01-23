/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.models.Resource;

@FunctionalInterface
public interface CelExpression<T> {

    /**
     * Evaluates this expression on the given resource.
     *
     * @param resource The resource - cannot be {@code null}.
     * @return the expression evaluation result.
     */
    T eval(Resource resource);
}
