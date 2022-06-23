/*
 * Copyright 2022 StreamThoughts.
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

import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.annotations.EnableAutoConfigure;
import io.streamthoughts.jikkou.api.model.ResourceTransformation;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(AutoApiConfigurator.class);


    /**
     * Creates a new {@link AutoApiConfigurator} instance.
     *
     * @param extensionFactory an {@link ExtensionFactory}.
     */
    public AutoApiConfigurator(@NotNull ExtensionFactory extensionFactory) {
        super(extensionFactory);
    }

    /** {@inheritDoc} **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {
        LOG.info("Loading resource validations for auto-configure enable");
        java.util.List<ResourceValidation> validations = extensionFactory()
                .allExtensionsDescriptorForType(ResourceValidation.class)
                .stream()
                .filter(descriptor -> AnnotationResolver.isAnnotatedWith(descriptor.clazz(), EnableAutoConfigure.class))
                .map(descriptor -> (ResourceValidation) getExtension(descriptor.classType()))
                .peek(extension -> LOG.info("Added {}", extension.name()))
                .toList();

        LOG.info("Loading resource transformations for auto-configure enable");
        java.util.List<ResourceTransformation> transformations = extensionFactory()
                .allExtensionsDescriptorForType(ResourceTransformation.class)
                .stream()
                .filter(descriptor -> AnnotationResolver.isAnnotatedWith(descriptor.clazz(), EnableAutoConfigure.class))
                .map(descriptor -> (ResourceTransformation) getExtension(descriptor.classType()))
                .peek(extension -> LOG.info("Added {}", extension.name()))
                .toList();

        return builder
                .withValidations(validations)
                .withTransformations(transformations);
    }
}
