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
package io.streamthoughts.jikkou.client.configure;

import io.streamthoughts.jikkou.api.BaseApiConfigurator;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.control.ResourceDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceDescriptorApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDescriptorApiConfigurator.class);

    public static final String DESCRIPTORS__ACTIVE_KEY = "descriptors.active";
    public static final ConfigProperty<java.util.List<String>> DESCRIPTORS_PROPERTY = ConfigProperty
            .ofList(DESCRIPTORS__ACTIVE_KEY);

    public ResourceDescriptorApiConfigurator(final ExtensionFactory extensionFactory) {
        super(extensionFactory);
    }

    /** {@inheritDoc} **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {

        LOG.info("Loading active resource descriptors");
        java.util.List<String> controllerClasses = getPropertyValue(DESCRIPTORS_PROPERTY);

        java.util.List<? extends ResourceDescriptor<?>> extensions = controllerClasses
                .stream()
                .peek(controllerClass -> LOG.info("Added {}", controllerClass))
                .map(controllerClass -> (ResourceDescriptor<?>) getExtension(controllerClass))
                .toList();

        B result = builder;
        for (ResourceDescriptor<?> extension : extensions) {
            result = result.withDescriptor(extension);
        }
        return result;
    }
}
