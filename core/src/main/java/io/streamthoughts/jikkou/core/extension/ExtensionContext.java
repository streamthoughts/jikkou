/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Extension context.
 *
 * @see Extension#init(ExtensionContext).
 */
public interface ExtensionContext {

    /**
     * Returns the name of the extension.
     *
     * @return The name.
     */
    String name();

    /**
     * Returns the application configuration.
     *
     * @return The configuration.
     */
    Configuration appConfiguration();

    /**
     * Gets the configuration properties by name as defined by the extension specification.
     *
     * @return The list of properties.
     */
    Map<String, ConfigProperty> configProperties();

    /**
     * Gets the configuration property for the specified name as defined by the extension specification.
     *
     * @param key The property key.
     * @param <T> The type of the config property.
     * @return The config property.
     * @throws IllegalArgumentException if the given key is null or empty.
     * @throws NoSuchElementException   if no configuration property is defined for the given key.
     */
    <T> ConfigProperty<T> configProperty(String key);

    /**
     * Gets a new extension context from the specified extension type.
     *
     * @param extension The extension type.
     * @return  The ExtensionContext.
     */
    ExtensionContext contextForExtension(Class<? extends Extension> extension);
}
