/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.BaseApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.reporter.ChangeReporter;
import io.streamthoughts.jikkou.core.reporter.ChangeReporterDecorator;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link ChangeReporter}
 * dynamically passed through the CLI configuration.
 */
public final class ChangeReporterApiConfigurator extends ExtensionApiConfigurator<ChangeReporter> {

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param registry an {@link ExtensionDescriptorRegistry}.
     */
    public ChangeReporterApiConfigurator(@NotNull ExtensionDescriptorRegistry registry) {
        super(registry, JikkouConfigProperties.REPORTERS_CONFIG);
    }

    /**
     * {@inheritDoc }
     **/
    @Override
    protected Supplier<ChangeReporter> getExtensionSupplier(@NotNull ExtensionConfigEntry configEntry,
                                                            @NotNull ExtensionDescriptor<ChangeReporter> descriptor) {
        return new ChangeReporterDecoratorSupplier(configEntry, descriptor.supplier());
    }

    private static final class ChangeReporterDecoratorSupplier implements Supplier<ChangeReporter> {

        private final Supplier<ChangeReporter> delegate;
        private final ExtensionConfigEntry configEntry;

        public ChangeReporterDecoratorSupplier(ExtensionConfigEntry configEntry,
                                               Supplier<ChangeReporter> delegate
        ) {
            this.delegate = delegate;
            this.configEntry = configEntry;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ChangeReporter get() {
            ChangeReporter extension = delegate.get();
            return new ChangeReporterDecorator(extension)
                    .name(configEntry.name())
                    .configuration(configEntry.config());
        }
    }
}
