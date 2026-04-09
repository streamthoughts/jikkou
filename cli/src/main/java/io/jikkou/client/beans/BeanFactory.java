/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.beans;

import io.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.jikkou.client.GlobalConfigurationContext;
import io.jikkou.client.context.ConfigurationContext;
import io.jikkou.core.ApiConfigurator;
import io.jikkou.core.DefaultApi;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.jikkou.core.extension.DefaultExtensionFactory;
import io.jikkou.core.extension.DefaultExtensionRegistry;
import io.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.jikkou.core.extension.ExtensionFactory;
import io.jikkou.core.io.writer.DefaultResourceWriter;
import io.jikkou.core.io.writer.ResourceWriter;
import io.jikkou.core.repository.LocalResourceRepository;
import io.jikkou.core.resource.DefaultResourceRegistry;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.core.template.ResourceTemplateRenderer;
import io.jikkou.runtime.JikkouContext;
import io.jikkou.runtime.configurator.ReporterApiConfigurator;
import io.jikkou.runtime.configurator.RepositoryApiConfigurator;
import io.jikkou.runtime.configurator.TransformationApiConfigurator;
import io.jikkou.runtime.configurator.ValidationApiConfigurator;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
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
            new ReporterApiConfigurator(registry),
            new RepositoryApiConfigurator(registry)
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
    public LocalResourceRepository localResourceRepository(Configuration configuration) {
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
            .withPreserveRawTags(false)
            .withFailOnUnknownTokens(false);
        renderer.configure(configuration);
        return new LocalResourceRepository(renderer);
    }
}
