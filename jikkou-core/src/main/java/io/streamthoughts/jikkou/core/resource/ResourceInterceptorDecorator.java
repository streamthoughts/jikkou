/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.converter.ResourceConverter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *  This class can be used to decorate an interceptor with a different name and priority.
 *
 * @see ResourceInterceptor
 */
@Evolving
public class ResourceInterceptorDecorator<E extends ResourceInterceptor, D extends ResourceInterceptorDecorator<E, D>> implements ResourceInterceptor {

    protected final E extension;
    private Integer priority;
    private String name;

    private Configuration configuration;

    /**
     * Creates a new {@link ResourceInterceptorDecorator} instance.
     *
     * @param extension the extension; must not be null.
     */
    public ResourceInterceptorDecorator(E extension) {
        this.extension = Objects.requireNonNull(extension, "extension must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        this.extension.configure(this.configuration.withFallback(config));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean canAccept(@NotNull ResourceType type) {
        return this.extension.canAccept(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull HasMetadata resource) {
        return this.extension.getResourceConverter(resource);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull ResourceType resource) {
        return this.extension.getResourceConverter(resource);
    }

    @SuppressWarnings("unchecked")
    public D withPriority(@Nullable Integer priority) {
        this.priority = priority;
        return (D) this;
    }

    @SuppressWarnings("unchecked")
    public D withName(@Nullable String name) {
        this.name = name;
        return (D) this;
    }

    @SuppressWarnings("unchecked")
    public D withConfiguration(@Nullable Configuration configuration) {
        this.configuration = configuration;
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
    public String getName() {
        return name != null ? name : extension.getName();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "[" +
                "extension=" + extension +
                ", priority=" + priority +
                ", name='" + name +
                ']';
    }
}
