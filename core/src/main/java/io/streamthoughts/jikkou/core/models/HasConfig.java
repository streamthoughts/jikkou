/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import java.util.List;

/**
 * Interface that can be implemented by classes accepting specific configuration properties.
 */
public interface HasConfig {

    /**
     * Gets the list of config properties supported by this class.
     * <p>
     * This method should return all the properties supported by the class.
     *
     * @return The list of properties.
     */
    default List<ConfigProperty<?>> configProperties() {
        return List.of();
    }
}
