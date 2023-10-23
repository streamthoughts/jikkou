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
public class ChangeReporterApiConfigurator extends ExtensionApiConfigurator<ChangeReporter> {

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
                    .withName(configEntry.name())
                    .withConfiguration(configEntry.config());
        }
    }
}
