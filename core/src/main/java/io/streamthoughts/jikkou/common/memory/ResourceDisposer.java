/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.memory;

/**
 * A {@code MemoryResourceDisposer} can be used to dispose a shared
 * resource after it is not used anymore.
 *
 * @see OpaqueMemoryResource
 */

@FunctionalInterface
public interface ResourceDisposer<E extends Throwable> {

    /**
     * Release the memory shared resource.
     */
    void dispose() throws E;
}
