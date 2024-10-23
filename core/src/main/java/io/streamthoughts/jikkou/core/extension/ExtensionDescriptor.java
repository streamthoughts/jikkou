/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Descriptor for a class that implements the {@link Extension} interface.
 */
public interface ExtensionDescriptor<T> extends Comparable<ExtensionDescriptor<T>> {

    /**
     * Gets the name of the extension.
     *
     * @return the name, or {@code null} if the name is not set.
     */
    String name();

    /**
     * Gets the short description of the extension.
     *
     * @return  The title.
     */
    String title();

    /**
     * Gets the description of the extension.
     *
     * @return the description, or {@code null} if the description is not set.
     */
    String description();

    /**
     * Gets the examples of the extension.
     *
     * @return The examples.
     */
    List<Example> examples();

    /**
     * Gets the specification of config properties of the extension.
     *
     * @return  The config property specs.
     */
    List<ConfigPropertySpec> properties();

    /**
     * Gets the priority of the extension.
     *
     * @return  The priority
     */
    Integer priority();

    /**
     * Gets the extension metadata.
     *
     * @return the {@link ExtensionMetadata}.
     */
    ExtensionMetadata metadata();

    /**
     * Gets the classloader used to load the extension.
     *
     * @return the {@link ClassLoader}.
     */
    ClassLoader classLoader();

    /**
     * Checks whether the extension is enabled.
     *
     * @return the {@link ClassLoader}.
     */
    boolean isEnabled();

    /**
     * Gets the category to which this extension belongs to.
     *
     * @return The category.
     */
    ExtensionCategory category();

    /**
     * Gets the provider to which this extension belongs to.
     *
     * @return The provider.
     */
    Class<? extends ExtensionProvider> provider();


    /**
     * Gets the supplier for the provider to which this extension belongs to.
     *
     * @return The provider.
     */
    Supplier<? extends ExtensionProvider> providerSupplier();

    /**
     * Adds new aliases to reference the described extension.
     *
     * @param aliases the aliases to be added.
     */
    void addAliases(final Set<String> aliases);

    /**
     * Gets the set of aliases for this extension.
     *
     * @return the aliases.
     */
    Set<String> aliases();

    /**
     * Gets the type of the described extension.
     *
     * @return the class of type {@code T}.
     */
    Class<T> type();

    /**
     * Gets the fully-qualified class name of the extension.
     *
     * @return the class name.
     */
    default String className() {
        return type().getName();
    }

    /**
     * Gets the supplier used to create a new extension of type {@link T}.
     *
     * @return the {@link Supplier}.
     */
    Supplier<T> supplier();

    /**
     * Gets the configuration to use for initializing the extension.
     *
     * @return the {@link Configuration}.
     */
    Configuration configuration();

    /**
     * {@inheritDoc}
     **/
    @Override
    default int compareTo(@NotNull ExtensionDescriptor<T> that) {
        return that.className().compareTo(this.className());
    }
}
