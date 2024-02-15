/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.memory;

/**
 * A {@code ResourceInitializer} is used to initialize a new resource.
 *
 * @param <T>   the resource type.
 */
@FunctionalInterface
public interface ResourceInitializer<T> {

    /**
     * Creates a new resource.
     *
     * @return  the resource.
     */
    T apply();
}
