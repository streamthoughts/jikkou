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
package io.streamthoughts.jikkou.api.extensions;

import io.streamthoughts.jikkou.annotation.ExtensionDescription;
import io.streamthoughts.jikkou.annotation.ExtensionEnabled;
import io.streamthoughts.jikkou.annotation.ExtensionName;
import io.streamthoughts.jikkou.annotation.ExtensionType;
import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;

/**
 * The top-level interface for extension.
 *
 * @see io.streamthoughts.jikkou.api.model.Resource
 * @see ResourceValidation
 * @see ResourceTransformation
 * @see ResourceController
 * @see ResourceCollector
 * @see io.streamthoughts.jikkou.api.health.HealthIndicator
 */
@Evolving
public interface Extension extends Configurable {

    /**
     * Get the runtime name of this extension.
     *
     * @return the extension name.
     */
    default String getName() {
        return getName(this);
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param extension the extension for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Extension extension) {
        return getName(extension.getClass());
    }

    /**
     * Get the static name of the given extension class.
     *
     * @param clazz the extension class for which to extract the name.
     * @return the extension name.
     */
    static String getName(final Class<? extends Extension> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, ExtensionName.class)
                .stream()
                .map(ExtensionName::value)
                .findFirst()
                .orElse(clazz.getSimpleName());
    }

    /**
     * Check whether the given extension is enabled.
     *
     * @param clazz the extension class for which to extract the description.
     * @return boolean, default is {@code true}.
     */
    static boolean isEnabled(final Class<? extends Extension> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, ExtensionEnabled.class)
                .stream()
                .map(ExtensionEnabled::value)
                .findFirst()
                .orElse(true);
    }

    /**
     * Gets the description of the given extension class.
     *
     * @param clazz the extension class for which to extract the description.
     * @return the description or {@code ""}.
     */
    static String getDescription(final Class<? extends Extension> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, ExtensionDescription.class)
                .stream()
                .map(ExtensionDescription::value)
                .findFirst()
                .orElse("");
    }

    /**
     * Gets the type of the given extension class.
     *
     * @param clazz the extension class for which to extract the type.
     * @return the type or {@code "<unknown>"}.
     */
    static String getType(final Class<? extends Extension> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, ExtensionType.class)
                .stream()
                .map(ExtensionType::value)
                .findFirst()
                .orElse("<unknown>");
    }
}
