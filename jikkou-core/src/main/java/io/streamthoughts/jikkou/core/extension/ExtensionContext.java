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
