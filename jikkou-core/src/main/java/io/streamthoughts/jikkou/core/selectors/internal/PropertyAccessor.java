/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.selectors.internal;

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