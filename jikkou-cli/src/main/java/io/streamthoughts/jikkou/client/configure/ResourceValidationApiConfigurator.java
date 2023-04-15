/*
 * Copyright 2023 StreamThoughts.
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

import static io.streamthoughts.jikkou.client.JikkouConfigProperty.ofConfigs;

import io.streamthoughts.jikkou.api.BaseApiConfigurator;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.client.JikkouConfig;
import io.streamthoughts.jikkou.common.utils.Tuple2;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceValidationApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceValidationApiConfigurator.class);

    public static final ConfigProperty<java.util.List<Tuple2<String, JikkouConfig>>> VALIDATIONS_PROPERTY = ofConfigs("validations")
            .map(configs -> configs.stream()
                    .map(o -> new JikkouConfig(o, false))
                    .map(config -> new Tuple2<>(
                            config.getString("type"),
                            config.findConfig("config").getOrElse(JikkouConfig.empty())
                    ))
                    .collect(Collectors.toList())
            );

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
        java.util.List<Tuple2<String, JikkouConfig>> extensionsClasses = getPropertyValue(VALIDATIONS_PROPERTY);

        java.util.List<ResourceValidation<HasMetadata>> extensions = extensionsClasses.stream()
                .peek(tuple -> LOG.info("Configure '{}' with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> {
                    String extensionType = tuple._1();
                    Configuration extensionConfig = tuple._2().withFallback(configuration());
                    return (ResourceValidation<HasMetadata>) extensionFactory().getExtension(extensionType, extensionConfig);
                }).toList();

        return builder.withValidations(extensions);
    }
}