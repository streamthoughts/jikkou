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
package io.streamthoughts.jikkou.api.transform;


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


/**
 * This class is used to configured {@link JikkouApi} with all {@link ResourceTransformation} dynamically
 * passed through the CLI configuration.
 */
public class ResourceTransformationApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTransformationApiConfigurator.class);

    public static final String TRANSFORMATIONS_CONFIG_NAME = "transformations";

    public static final ConfigProperty<List<ExtensionConfigDescriptor>> TRANSFORMATION_CONFIG = ConfigProperty
            .ofConfigList(TRANSFORMATIONS_CONFIG_NAME)
            .map(configs -> configs.stream().map(ExtensionConfigDescriptor::of).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    /**
     * Creates a new {@link ResourceTransformationApiConfigurator} instance.
     *
     * @param extensionFactory the extension factory.
     */
    public ResourceTransformationApiConfigurator(final ExtensionFactory extensionFactory) {
        super(extensionFactory);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {

        LOG.info("Loading all resource transformations from config settings");
        List<ExtensionConfigDescriptor> extensions = getPropertyValue(TRANSFORMATION_CONFIG);
        List<ResourceTransformation<HasMetadata>> transformations = extensions.stream()
                .peek(extension -> LOG.info(
                        "Configure transformation for type {} (name={}, priority={}):\n\t{}",
                        extension.extensionClass(),
                        extension.name(),
                        extension.priority(),
                        extension.config().toPrettyString("\n\t")))
                .map(extension -> {
                    String extensionClass = extension.extensionClass();
                    Configuration extensionConfig = extension.config().withFallback(configuration());
                    ResourceTransformation<HasMetadata> transformation = extensionFactory()
                            .getExtension(extensionClass, extensionConfig);

                    transformation = new ResourceTransformationDecorator<>(transformation)
                            .withName(extension.name())
                            .withPriority(extension.priority());
                    return transformation;
                }).toList();

        return builder.withTransformations(transformations);
    }
}