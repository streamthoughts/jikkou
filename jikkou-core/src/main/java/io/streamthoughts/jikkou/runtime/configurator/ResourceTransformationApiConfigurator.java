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
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformationDecorator;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link ResourceTransformation}
 * dynamically passed through the CLI configuration.
 */
public class ResourceTransformationApiConfigurator extends ExtensionApiConfigurator<ResourceTransformation<?>> {

    public static final String TRANSFORMATIONS_CONFIG_NAME = "transformations";

    public static final ConfigProperty<List<ExtensionConfigEntry>> TRANSFORMATION_CONFIG = ConfigProperty
            .ofConfigList(TRANSFORMATIONS_CONFIG_NAME)
            .map(configs -> configs.stream().map(ExtensionConfigEntry::of).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    /**
     * Creates a new {@link ResourceTransformationApiConfigurator} instance.
     *
     * @param registry the ExtensionDescriptorRegistry instance.
     */
    public ResourceTransformationApiConfigurator(final ExtensionDescriptorRegistry registry) {
        super(registry, TRANSFORMATION_CONFIG);
    }

    /**
     * {@inheritDoc}
     **/
       @Override
    protected Supplier<ResourceTransformation<?>> getExtensionSupplier(ExtensionConfigEntry configEntry,
                                                                       ExtensionDescriptor<ResourceTransformation<?>> descriptor) {
           return new TransformationDecoratorSupplier(configEntry, descriptor.supplier());
    }

    private static final class TransformationDecoratorSupplier implements Supplier<ResourceTransformation<?>> {

        private final Supplier<ResourceTransformation<?>> delegate;
        private final ExtensionConfigEntry configEntry;

        public TransformationDecoratorSupplier(ExtensionConfigEntry configEntry,
                                               Supplier<ResourceTransformation<?>> delegate) {
            this.delegate = delegate;
            this.configEntry = configEntry;
        }

        /** {@inheritDoc} **/
        @Override
        public ResourceTransformation<?> get() {
            ResourceTransformation<?> extension = delegate.get();
            extension = new ResourceTransformationDecorator<>(extension)
                    .withPriority(configEntry.priority())
                    .withName(configEntry.name())
                    .withConfiguration(configEntry.config());
            return extension;
        }
    }
}