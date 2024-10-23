/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.beans;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.client.GlobalConfigurationContext;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoaderFacade;
import io.streamthoughts.jikkou.core.io.writer.DefaultResourceWriter;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import io.streamthoughts.jikkou.runtime.configurator.ChangeReporterApiConfigurator;
import io.streamthoughts.jikkou.runtime.configurator.TransformationApiConfigurator;
import io.streamthoughts.jikkou.runtime.configurator.ValidationApiConfigurator;
import jakarta.inject.Singleton;

/**
 * Factory class.
 */
@Factory
public final class BeanFactory {

    @Singleton
    public ExtensionDescriptorRegistry extensionDescriptorRegistry() {
        return new DefaultExtensionRegistry(
            new DefaultExtensionDescriptorFactory(),
            new ClassExtensionAliasesGenerator()
        );
    }

    @Singleton
    public ExtensionFactory extensionFactory(ExtensionDescriptorRegistry registry) {
        return new DefaultExtensionFactory(registry);
    }

    @Singleton
    public JikkouContext jikkouContext(Configuration configuration,
                                       ExtensionFactory factory,
                                       ResourceRegistry resourceRegistry) {
        return new JikkouContext(configuration, factory, resourceRegistry);
    }

    @Singleton
    public ResourceRegistry resourceRegistry() {
        return new DefaultResourceRegistry();
    }

    @Singleton
    @SuppressWarnings({"rawtypes", "unchecked"})
    public JikkouApi jikkouApi(JikkouContext context,
                               ExtensionDescriptorRegistry registry,
                               JikkouApi.ApiBuilder apiBuilder) {
        ApiConfigurator[] configurators = {
            new ValidationApiConfigurator(registry),
            new TransformationApiConfigurator(registry),
            new ChangeReporterApiConfigurator(registry)
        };
        return context.createApi(apiBuilder, configurators);
    }

    @Singleton
    @Requires(missingBeans = ProxyConfiguration.class)
    @SuppressWarnings("rawtypes")
    public JikkouApi.ApiBuilder defaultApiBuilder(JikkouContext context) {
        return new DefaultApi.Builder(context.getExtensionFactory(), context.getResourceRegistry());
    }

    @Singleton
    public ConfigurationContext configurationContext() {
        return GlobalConfigurationContext.getConfigurationContext();
    }

    @Singleton
    public Configuration configuration() {
        return GlobalConfigurationContext.getConfiguration();
    }

    @Singleton
    public ResourceWriter defaultResourceWriter() {
        return new DefaultResourceWriter();
    }

    @Singleton
    public ResourceLoaderFacade resourceLoaderFacade(Configuration configuration) {
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
            .withPreserveRawTags(false)
            .withFailOnUnknownTokens(false);
        renderer.configure(configuration);
        return new ResourceLoaderFacade(renderer, Jackson.YAML_OBJECT_MAPPER);
    }
}
