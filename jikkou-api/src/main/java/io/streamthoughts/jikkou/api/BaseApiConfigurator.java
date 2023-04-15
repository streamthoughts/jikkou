/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import java.util.Collection;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class BaseApiConfigurator implements ApiConfigurator {

    private final ExtensionFactory extensionFactory;

    private Configuration configuration;

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param extensionFactory  an {@link ExtensionFactory}.
     */
    public BaseApiConfigurator(final @NotNull ExtensionFactory extensionFactory) {
        this.extensionFactory = Objects.requireNonNull(
                extensionFactory,
                "extensionFactory must not be null"
        );
    }

    protected ExtensionFactory extensionFactory() {
        return extensionFactory;
    }

    protected Configuration configuration() {
        return configuration;
    }

    protected <T> T getPropertyValue(final ConfigProperty<T> property) {
        return property.evaluate(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(
            @NotNull final B builder,
            @NotNull final Configuration configuration) {
        this.configuration = configuration;
        return configure(builder);
    }

    public abstract <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder);

    public <T extends Extension> T getExtension(final String classType) {
        return extensionFactory.getExtension(classType, configuration);
    }

    public <T extends Extension> Collection<T> getExtension(final Class<T> classType) {
        return extensionFactory.getAllExtensions(classType, configuration);
    }
}
