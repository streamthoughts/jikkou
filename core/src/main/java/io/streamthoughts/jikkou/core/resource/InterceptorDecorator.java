/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.extension.ExtensionDecorator;
import io.streamthoughts.jikkou.core.models.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be used to decorate an interceptor with a different name and priority.
 *
 * @see Interceptor
 */
@Evolving
public class InterceptorDecorator<E extends Interceptor, D extends InterceptorDecorator<E, D>> extends ExtensionDecorator<E, D> implements Interceptor {

    private Integer priority;

    /**
     * Creates a new {@link InterceptorDecorator} instance.
     *
     * @param extension the extension; must not be null.
     */
    public InterceptorDecorator(E extension) {
        super(extension);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean canAccept(@NotNull ResourceType type) {
        return this.extension.canAccept(type);
    }

    @SuppressWarnings("unchecked")
    public D priority(@Nullable Integer priority) {
        this.priority = priority;
        return (D) this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int getPriority() {
        return priority != null ? priority : extension.getPriority();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "[" +
                "extension=" + extension +
                ", priority=" + priority +
                ", name='" + getName() +
                ']';
    }
}
