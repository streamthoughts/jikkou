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

import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
     * Gets the description of the extension.
     *
     * @return the description, or {@code null} if the description is not set.
     */
    String description();

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
     * Gets the category of the extension.
     */
    String category();

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
     * {@inheritDoc}
     **/
    @Override
    default int compareTo(@NotNull ExtensionDescriptor<T> that) {
        return that.className().compareTo(this.className());
    }

    default List<ResourceType> supportedResources() {
        return HasMetadataAcceptable.getAcceptedResources(type())
                .stream()
                .toList();
    }

    default String printableSupportedResources() {
        return supportedResources()
                .stream()
                .map(ResourceType::kind)
                .collect(Collectors.joining(", "));
    }

    default String source() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof ExtensionClassLoader o) {
            return o.location();
        }
        return "<internal>";
    }

    default String printableAliases() {
        return String.join(", ", aliases());
    }
}
