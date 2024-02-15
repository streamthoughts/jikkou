/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationDecorator;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link Validation}
 * dynamically passed through the CLI configuration.
 */
public final class ValidationApiConfigurator extends ExtensionApiConfigurator<Validation<?>> {

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
    protected Supplier<Validation<?>> getExtensionSupplier(final @NotNull ExtensionConfigEntry extension,
                                                           final @NotNull ExtensionDescriptor<Validation<?>> descriptor) {
        return new ValidationDecoratorSupplier(descriptor.supplier(), extension);
    }

    private static final class ValidationDecoratorSupplier implements Supplier<Validation<?>> {

        private final Supplier<Validation<?>> delegate;
        private final ExtensionConfigEntry configEntry;

        public ValidationDecoratorSupplier(Supplier<Validation<?>> delegate,
                                           ExtensionConfigEntry configEntry) {
            this.delegate = delegate;
            this.configEntry = configEntry;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Validation<?> get() {
            Validation<?> extension = delegate.get();
            extension = new ValidationDecorator<>(extension)
                    .priority(configEntry.priority())
                    .name(configEntry.name())
                    .configuration(configEntry.config());
            return extension;
        }
    }
}