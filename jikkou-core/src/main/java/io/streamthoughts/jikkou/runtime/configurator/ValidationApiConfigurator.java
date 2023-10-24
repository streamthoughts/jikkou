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
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidation;
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidationDecorator;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link ResourceValidation}
 * dynamically passed through the CLI configuration.
 */
public final class ValidationApiConfigurator extends ExtensionApiConfigurator<ResourceValidation<?>> {

    /**
     * Creates a new {@link ValidationApiConfigurator} instance.
     *
     * @param registry the ExtensionDescriptorRegistry instance.
     */
    public ValidationApiConfigurator(final ExtensionDescriptorRegistry registry) {
        super(registry, JikkouConfigProperties.VALIDATIONS_CONFIG);
    }

    /** {@inheritDoc}**/
    @Override
    protected Supplier<ResourceValidation<?>> getExtensionSupplier(final @NotNull ExtensionConfigEntry extension,
                                                                   final @NotNull ExtensionDescriptor<ResourceValidation<?>> descriptor) {
        return new ValidationDecoratorSupplier(descriptor.supplier(), extension);
    }

    private static final class ValidationDecoratorSupplier implements Supplier<ResourceValidation<?>> {

        private final Supplier<ResourceValidation<?>> delegate;
        private final ExtensionConfigEntry configEntry;

        public ValidationDecoratorSupplier(Supplier<ResourceValidation<?>> delegate,
                                           ExtensionConfigEntry configEntry) {
            this.delegate = delegate;
            this.configEntry = configEntry;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceValidation<?> get() {
            ResourceValidation<?> extension = delegate.get();
            extension = new ResourceValidationDecorator<>(extension)
                    .withPriority(configEntry.priority())
                    .withName(configEntry.name())
                    .withConfiguration(configEntry.config());
            return extension;
        }
    }
}