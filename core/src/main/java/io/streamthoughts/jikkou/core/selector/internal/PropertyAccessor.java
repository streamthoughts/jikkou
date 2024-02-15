/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector.internal;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;

public interface PropertyAccessor {

    /**
     * Returns {@code null} which means this is a general purpose accessor.
     *
     * @return the array of {@link Class} supported type.
     */
    default Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    /**
     * Checks whether this accessor can read the specified variable name.
     *
     * @param target    the target object.
     * @param name      the variable name.
     *
     * @return  {@code true} if is readable.
     *
     * @throws SelectorException if an error occurred while evaluating the variable.
     */
    boolean canRead(final Object target,
                    final String name) throws SelectorException;

    /**
     * Reads the specified variable from the target object.
     *
     * @param target    the target object.
     * @param name      the variable name to read.
     *
     * @return the value.
     *
     * @throws SelectorException if an error occurred while evaluating the variable.
     */
    Object read(final Object target,
                final String name) throws SelectorException;

}