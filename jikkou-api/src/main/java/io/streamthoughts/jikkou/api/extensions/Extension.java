/*
 * Copyright 2021 StreamThoughts.
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

import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.extensions.annotations.ExtensionDescription;
import io.streamthoughts.jikkou.api.extensions.annotations.ExtensionType;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;

/**
 * The top-level interface for extension.
 *
 * @see io.streamthoughts.jikkou.api.model.Resource
 * @see io.streamthoughts.jikkou.api.model.ResourceValidation
 * @see io.streamthoughts.jikkou.api.model.ResourceTransformation
 * @see io.streamthoughts.jikkou.api.control.ResourceController
 * @see io.streamthoughts.jikkou.api.control.ResourceDescriptor
 * @see io.streamthoughts.jikkou.api.health.HealthIndicator
 */
@InterfaceStability.Evolving
public interface Extension extends Configurable {

    /**
     * Gets the description of the given extension class.
     *
     * @param clazz the extension class for which to extract the description.
     * @return      the description or {@code null}.
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
     * @return      the Version or {@code null}.
     */
    static String getType(final Class<? extends Extension> clazz) {
        return AnnotationResolver.findAllAnnotationsByType(clazz, ExtensionType.class)
                .stream()
                .map(ExtensionType::value)
                .findFirst()
                .orElse("<unknown>");
    }
}
