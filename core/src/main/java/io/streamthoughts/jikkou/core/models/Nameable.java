/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

/**
 * A nameable object.
 */
public interface Nameable<T> {

    /**
     * Gets the object name.
     *
     * @return The name.
     */
    String getName();

    /**
     * Returns the object with the given name.
     *
     * @param name  The name.
     * @return  The nameable object.
     */
    T withName(String name);
}
