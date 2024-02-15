/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

/**
 * Interface for defining a resource with a specification.
 *
 * @param <T> type of the specification.
 */
public interface HasSpec<T> extends HasMetadata {

    /**
     * Returns the resource specification.
     *
     * @return the specification.
     */
    T getSpec();

}