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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtensionDecorator<E extends Extension, T extends ExtensionDecorator<E, T>> implements Extension {

    protected final E extension;

    private String name;
    private Configuration configuration;

    /**
     * Creates a new {@link ExtensionDecorator} instance.
     *
     * @param extension The extension.
     */
    public ExtensionDecorator(@NotNull E extension) {
        this.extension = extension;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) throws ConfigException {
        final String name = getName();
        this.extension.init(new ExtensionContextDecorator(context) {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Configuration appConfiguration() {
                return context.appConfiguration().withFallback(configuration);
            }
        });
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String getName() {
        return name != null ? name : extension.getName();
    }

    /**
     * Sets the name of the extension.
     *
     * @param name The extension name.
     * @return {@code this}.
     */
    public T name(@Nullable String name) {
        this.name = name;
        return self();
    }

    /**
     * Sets the application configuration to passed to the of the extension through the init method.
     *
     * @param configuration The Configuration.
     * @return {@code this}.
     */
    public T configuration(@Nullable Configuration configuration) {
        this.configuration = configuration;
        return self();
    }

    @NotNull
    protected T self() {
        return (T) this;
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return extension.getClass().getSimpleName() + " [name=" + getName() + ']';
    }
}
