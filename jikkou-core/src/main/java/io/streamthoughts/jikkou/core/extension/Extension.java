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
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.config.ConfigPropertyDescriptor;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionConfigProperties;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The top-level interface for extension.
 *
 * @see Resource
 * @see Validation
 * @see Transformation
 * @see Controller
 * @see Collector
 * @see HealthIndicator
 */
@Evolving
public interface Extension {

    /**
     * Get the runtime name of this extension.
     *
     * @return the extension name.
     */
    default String getName() {
        return getName(this);
    }

    /**
     * Gets the default configuration of this extension.
     *
     * @return  the configuration.
     */
    default Configuration getDefaultConfiguration() {
        List<ConfigPropertyDescriptor> descriptors = getExtensionConfigPropertySpec(this);
        Map<String, String> defaultProperties = descriptors
                .stream()
                .filter(Predicate.not(ConfigPropertyDescriptor::isRequired))
                .collect(Collectors.toMap(ConfigPropertyDescriptor::name, ConfigPropertyDescriptor::defaultValue));
        return Configuration.from(defaultProperties);
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param extension the extension for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Object extension) {
        return getName(extension.getClass());
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param clazz the extension class for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Class<?> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, Named.class)
                .stream()
                .map(Named::value)
                .findFirst()
                .orElse(clazz.getSimpleName());
    }

    /**
     * Check whether the given extension is enabled.
     *
     * @param clazz the extension class for which to extract the description.
     * @return boolean, default is {@code true}.
     */
    static boolean isEnabled(final Class<?> clazz) {
        return AnnotationResolver.isAnnotatedWith(clazz, Enabled.class);
    }

    /**
     * Gets the description of the given extension class.
     *
     * @param clazz the extension class for which to extract the description.
     * @return the description or {@code ""}.
     */
    static String getDescription(final Class<?> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, Description.class)
                .stream()
                .map(Description::value)
                .findFirst()
                .orElse("");
    }

    /**
     * Gets the category of the given extension class.
     *
     * @param clazz the extension class for which to extract the type.
     * @return the type or {@code "<unknown>"}.
     */
    static String getCategory(final Class<?> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, Category.class)
                .stream()
                .map(Category::value)
                .findFirst()
                .orElse("<unknown>");
    }

    /**
     * Gets the specifications for the static configuration properties supported by the specified extension object.
     *
     * @param extension the extension object.
     *
     * @return  the list of {@link ConfigPropertyDescriptor}.
     */
    static List<ConfigPropertyDescriptor> getExtensionConfigPropertySpec(Object extension) {
        return getExtensionConfigPropertySpec(extension.getClass());
    }

    /**
     * Gets the specifications for the static configuration properties supported by the specified extension class.
     *
     * @param clazz the extension class.
     * @return  the list of {@link ConfigPropertyDescriptor}.
     */
    static List<ConfigPropertyDescriptor> getExtensionConfigPropertySpec(Class<?> clazz) {
        ExtensionConfigProperties annotation = clazz.getAnnotation(ExtensionConfigProperties.class);
        if (annotation == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(annotation.properties())
                .map(descriptor -> new ConfigPropertyDescriptor(
                        descriptor.name(),
                        descriptor.type(),
                        descriptor.description(),
                        descriptor.defaultValue(),
                        descriptor.isRequired()
                ))
                .toList();
    }
}
