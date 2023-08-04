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
package io.streamthoughts.jikkou.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.streamthoughts.jikkou.api.ApiConfigurator;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.JikkouContext;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.YAMLResourceLoader;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.api.transform.ResourceTransformationApiConfigurator;
import io.streamthoughts.jikkou.api.validation.ResourceValidationApiConfigurator;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import jakarta.inject.Singleton;

/**
 * Factory class.
 */
@Factory
public final class BeanFactory {

    @Bean
    @Singleton
    public JikkouApi jikkouApi(JikkouContext context) {
        ApiConfigurator[] configurators = {
                new ResourceValidationApiConfigurator(context.getExtensionFactory()),
                new ResourceTransformationApiConfigurator(context.getExtensionFactory())
        };
        return context.createApi(configurators);
    }

    @Bean
    @Singleton
    public ConfigurationContext configurationContext(ObjectMapper mapper) {
        return new ConfigurationContext(mapper);
    }

    @Bean
    @Singleton
    public JikkouConfig configuration(ConfigurationContext context) {
        return context.getCurrentContext().load();
    }

    @Bean
    @Singleton
    public JikkouContext jikkouContext(JikkouConfig configuration) {
        return new JikkouContext(configuration);
    }

    @Bean
    @Singleton
    public YAMLResourceWriter resourceWriter() {
        return new YAMLResourceWriter(Jackson.YAML_OBJECT_MAPPER);
    }

    @Bean
    @Singleton
    public YAMLResourceLoader resourceLoader() {
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
                .withPreserveRawTags(false)
                .withFailOnUnknownTokens(false);
        return new YAMLResourceLoader(renderer, Jackson.YAML_OBJECT_MAPPER);
    }
}
