/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.annotation.Named;
import java.util.Optional;

public interface HasName {

    /**
     * Get the runtime name of this extension.
     *
     * @return the extension name.
     */
    default String getName() {
        return getName(this);
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param extension the extension for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Object extension) {
        return getName(extension.getClass());
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param clazz the extension class for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(Named.class))
                .map(Named::value)
                .orElse(clazz.getSimpleName());
    }

}
