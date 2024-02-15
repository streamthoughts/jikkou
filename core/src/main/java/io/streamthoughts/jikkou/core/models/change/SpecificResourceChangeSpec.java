/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

/**
 * A specific {@link ResourceChangeSpec}.
 *
 * @param <T> Type of the data.
 */
public interface SpecificResourceChangeSpec<T> extends ResourceChangeSpec {

    /**
     * Gets the data.
     *
     * @return The data.
     */
    T getData();

}
