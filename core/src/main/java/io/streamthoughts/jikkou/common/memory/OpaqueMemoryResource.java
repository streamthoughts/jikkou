/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.memory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@code OpaqueMemoryResource} represents a shared memory resource.
 *
 * @param <T>   the resource type.
 */
public class OpaqueMemoryResource<T> implements AutoCloseable {

    private final T resourceHandle;

    private final ResourceDisposer<Exception> disposer;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Creates a new {@link OpaqueMemoryResource} instance.
     *
     * @param resourceHandle  the resourceHandle to handle.
     * @param disposer  the {@link ResourceDisposer} to be used for releasing the resourceHandle.
     */
    public OpaqueMemoryResource(final T resourceHandle,
                                final ResourceDisposer<Exception> disposer) {
        this.resourceHandle = Objects.requireNonNull(resourceHandle, "resourceHandle must not be null");;
        this.disposer = Objects.requireNonNull(disposer, "disposer must not be null");
    }

    /** Gets the handle to the resource. */
    public T getResourceHandle() {
        return resourceHandle;
    }

    /**
     * Releases this resource.
     */
    @Override
    public void close() throws Exception{
        if (closed.compareAndSet(false, true)) {
            disposer.dispose();
        }
    }

    @Override
    public String toString() {
        return "OpaqueMemoryResource @ "
                + resourceHandle
                + (closed.get() ? " (disposed)" : "");
    }
}