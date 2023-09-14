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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.model.HasPriority;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an extension configuration.
 */
public final class ExtensionConfigDescriptor {

    public static final ConfigProperty<String> NAME_CONFIG = ConfigProperty
            .ofString("name")
            .description("The name of the configured extension");
    public static final ConfigProperty<String> TYPE_CONFIG = ConfigProperty
            .ofString("type")
            .description("The type or fully qualified class name of the extension");
    public static final ConfigProperty<Integer> PRIORITY_CONFIG = ConfigProperty
            .ofInt("priority")
            .description("The priority order of the extension")
            .orElse(HasPriority.NO_ORDER);
    public static final ConfigProperty<Configuration> CONFIGURATION_CONFIG = ConfigProperty
            .ofConfig("config")
            .description("The configuration of the extension")
            .orElse(Configuration.empty());

    public static ExtensionConfigDescriptor of(final @NotNull Configuration config) {
        Objects.requireNonNull(config, "config must not be null");
        return new ExtensionConfigDescriptor(
                NAME_CONFIG.evaluate(config),
                TYPE_CONFIG.evaluate(config),
                PRIORITY_CONFIG.evaluate(config),
                CONFIGURATION_CONFIG.evaluate(config)
        );
    }

    private final String name;
    private final String extensionClass;
    private final Integer priority;
    private final Configuration config;

    /**
     * Creates a new {@link ExtensionConfigDescriptor} instance.
     *
     * @param name  the name of the extension. Must not be null.
     * @param extensionClass the class of the extension (or alias). Must not be null.
     * @param priority  the extension priority. May be {@code null}.
     * @param config    the extension configuration. May be {@code null}.
     */
    public ExtensionConfigDescriptor(String name,
                                     String extensionClass,
                                     Integer priority,
                                     Configuration config) {
        this.name = name;
        this.extensionClass = extensionClass;
        this.priority = priority;
        this.config = config;
    }

    /**
     * Gets the name of the configured extension.
     * @return  the string name.
     */
    public String name() {
        return name;
    }

    /**
     * Gets the class name of the configured extension.
     * @return  the string class name.
     */
    public String extensionClass() {
        return extensionClass;
    }

    /**
     * Gets the priority of the extension.
     * @return  the priority, or {@code null} if no priority was configured.
     */
    public Integer priority() {
        return priority;
    }

    /**
     * Gets the config of the extensions.
     * @return the {@link Configuration}, or {@code null} if no configuration was configured.
     */
    public Configuration config() {
        return config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionConfigDescriptor that = (ExtensionConfigDescriptor) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(extensionClass, that.extensionClass) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(config, that.config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(name, extensionClass, priority, config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "ExtensionConfig{" +
                "name='" + name + '\'' +
                ", extensionClass='" + extensionClass + '\'' +
                ", priority=" + priority +
                ", config=" + config +
                '}';
    }
}
