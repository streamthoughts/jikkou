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
package io.streamthoughts.jikkou.api.extensions;


import io.streamthoughts.jikkou.api.config.Configuration;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an extension configuration.
 */
public final class ResourceInterceptorDescriptor {

    public static final String NAME_CONFIG = "name";
    public static final String TYPE_CONFIG = "type";
    public static final String PRIORITY_CONFIG = "priority";
    public static final String CONFIGURATION_CONFIG = "config";

    public static ResourceInterceptorDescriptor of(final @NotNull Configuration config) {
        Objects.requireNonNull(config, "config must not be null");
        return new ResourceInterceptorDescriptor(
                config.getString(NAME_CONFIG),
                config.getString(TYPE_CONFIG),
                config.getInteger(PRIORITY_CONFIG),
                config.findConfig(CONFIGURATION_CONFIG).orElse(Configuration.empty())
        );
    }

    private final String name;
    private final String extensionClass;
    private final Integer priority;
    private final Configuration config;

    /**
     * Creates a new {@link ResourceInterceptorDescriptor} instance.
     *
     * @param name  the name of the extension. Must not be null.
     * @param extensionClass the class of the extension (or alias). Must not be null.
     * @param priority  the extension priority. May be {@code null}.
     * @param config    the extension configuration. May be {@code null}.
     */
    public ResourceInterceptorDescriptor(String name,
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
        ResourceInterceptorDescriptor that = (ResourceInterceptorDescriptor) o;
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
