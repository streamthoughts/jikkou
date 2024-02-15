/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;

@InterfaceStability.Evolving
public interface HasConfigs {

    /**
     * Gets the configs, may be null.
     *
     * @return  {@link Configs} object.
     */
    Configs getConfigs();

    /**
     * Sets the configs.
     *
     * @param configs the {@link Configs} to be set.
     */
    void setConfigs(final Configs configs);
    
}
