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
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.transform.TransformationDecorator;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.function.Supplier;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link Transformation}
 * dynamically passed through the CLI configuration.
 */
public final class TransformationApiConfigurator extends ExtensionApiConfigurator<Transformation<?>> {

    /**
     * Creates a new {@link TransformationApiConfigurator} instance.
     *
     * @param registry the ExtensionDescriptorRegistry instance.
     */
    public TransformationApiConfigurator(final ExtensionDescriptorRegistry registry) {
        super(registry, JikkouConfigProperties.TRANSFORMATION_CONFIG);
    }

    /**
     * {@inheritDoc}
     **/
       @Override
    protected Supplier<Transformation<?>> getExtensionSupplier(ExtensionConfigEntry configEntry,
                                                               ExtensionDescriptor<Transformation<?>> descriptor) {
           return new TransformationDecoratorSupplier(configEntry, descriptor.supplier());
    }

    private static final class TransformationDecoratorSupplier implements Supplier<Transformation<?>> {

        private final Supplier<Transformation<?>> delegate;
        private final ExtensionConfigEntry configEntry;

        public TransformationDecoratorSupplier(ExtensionConfigEntry configEntry,
                                               Supplier<Transformation<?>> delegate) {
            this.delegate = delegate;
            this.configEntry = configEntry;
        }

        /** {@inheritDoc} **/
        @Override
        public Transformation<?> get() {
            Transformation<?> extension = delegate.get();
            extension = new TransformationDecorator<>(extension)
                    .priority(configEntry.priority())
                    .name(configEntry.name())
                    .configuration(configEntry.config());
            return extension;
        }
    }
}