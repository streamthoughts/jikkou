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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.api.BaseApiConfigurator;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionConfigDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceValidationApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceValidationApiConfigurator.class);

    public static final String VALIDATIONS_CONFIG_NAME = "validations";
    public static final ConfigProperty<List<ExtensionConfigDescriptor>> VALIDATIONS_CONFIG = ConfigProperty
            .ofConfigList(VALIDATIONS_CONFIG_NAME)
            .map(configs -> configs.stream().map(ExtensionConfigDescriptor::of).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    /**
     * Creates a new {@link ResourceValidationApiConfigurator} instance.
     * @param extensionFactory  the extension factory.
     */
    public ResourceValidationApiConfigurator(final ExtensionFactory extensionFactory) {
        super(extensionFactory);
    }

    /** {@inheritDoc} **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {
        LOG.info("Loading all resource validations from config settings");
        List<ExtensionConfigDescriptor> extensions = getPropertyValue(VALIDATIONS_CONFIG);
        List<ResourceValidation<HasMetadata>> validations = extensions.stream()
                .peek(extension -> LOG.info(
                        "Configure validation for type {} (name={}, priority={}):\n\t{}",
                        extension.extensionClass(),
                        extension.name(),
                        extension.priority(),
                        extension.config().toPrettyString("\n\t")))
                .map(extension -> {
                    String extensionClass = extension.extensionClass();
                    Configuration extensionConfig = extension.config().withFallback(configuration());
                    ResourceValidation<HasMetadata> validation = extensionFactory()
                            .getExtension(extensionClass, extensionConfig);

                    validation = new ResourceValidationDecorator<>(validation)
                            .withPriority(extension.priority())
                            .withName(extension.name());

                    return validation;
                }).toList();

        return builder.withValidations(validations);
    }
}