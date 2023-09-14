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
package io.streamthoughts.jikkou.api.reporter;

import io.streamthoughts.jikkou.api.BaseApiConfigurator;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionConfigDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeReporterApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeReporterApiConfigurator.class);

    public static final String REPORTERS_CONFIG_NAME = "reporters";
    public static final ConfigProperty<List<ExtensionConfigDescriptor>> REPORTERS_CONFIG = ConfigProperty
            .ofConfigList(REPORTERS_CONFIG_NAME)
            .map(configs -> configs.stream().map(ExtensionConfigDescriptor::of).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param extensionFactory an {@link ExtensionFactory}.
     */
    public ChangeReporterApiConfigurator(@NotNull ExtensionFactory extensionFactory) {
        super(extensionFactory);
    }

    /** {@inheritDoc } **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {
        LOG.info("Loading all resource change reporter from config settings");
        List<ExtensionConfigDescriptor> extensions = getPropertyValue(REPORTERS_CONFIG);
        List<ChangeReporter> reporters = extensions.stream()
                .peek(extension -> LOG.info(
                        "Configure resource change reporter for type {} (name={}, priority={}):\n\t{}",
                        extension.extensionClass(),
                        extension.name(),
                        extension.priority(),
                        extension.config().toPrettyString("\n\t")))
                .map(extension -> {
                    String extensionClass = extension.extensionClass();
                    Configuration extensionConfig = extension.config();
                    ChangeReporter reporter = extensionFactory()
                            .getExtension(extensionClass, extensionConfig);

                    reporter = new ChangeReporterDecorator(reporter)
                            .withName(extension.name());
                    return reporter;
                }).toList();
        return builder.withReporters(reporters);
    }
}
