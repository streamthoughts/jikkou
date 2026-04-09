/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.beans;

import io.jikkou.core.ApiConfigurator;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.jikkou.core.extension.DefaultExtensionFactory;
import io.jikkou.core.extension.DefaultExtensionRegistry;
import io.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.jikkou.core.extension.ExtensionFactory;
import io.jikkou.core.models.ApiHealthIndicatorList;
import io.jikkou.core.resource.DefaultResourceRegistry;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.core.selector.SelectorFactory;
import io.jikkou.rest.configs.security.SecurityConfiguration;
import io.jikkou.rest.health.indicator.JikkouHealthIndicator;
import io.jikkou.rest.health.indicator.JikkouHealthIndicatorConfiguration;
import io.jikkou.runtime.JikkouConfig;
import io.jikkou.runtime.JikkouContext;
import io.jikkou.runtime.configurator.ReporterApiConfigurator;
import io.jikkou.runtime.configurator.RepositoryApiConfigurator;
import io.jikkou.runtime.configurator.TransformationApiConfigurator;
import io.jikkou.runtime.configurator.ValidationApiConfigurator;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;

@Factory
public final class BeanFactory {

    @Inject
    SecurityConfiguration securityConfiguration;

    @Property(name = "jikkou")
    @MapFormat(
            transformation = MapFormat.MapTransformation.NESTED,
            keyFormat = StringConvention.RAW
    )
    private Map<String, Object> configuration; // the loaded configuration of the application

    @Singleton
    public Configuration configuration() {
        return JikkouConfig.create(configuration);
    }

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
    public JikkouApi jikkouApi(JikkouContext context, ExtensionDescriptorRegistry registry) {
        ApiConfigurator[] configurators = {
                new ValidationApiConfigurator(registry),
                new TransformationApiConfigurator(registry),
                new ReporterApiConfigurator(registry),
                new RepositoryApiConfigurator(registry)
        };
        return context.createApi(configurators);
    }

    @Singleton
    public SelectorFactory expressionResourceSelectorFactory() {
        return new SelectorFactory();
    }

    @Singleton
    @Requirements({@Requires(
            property = "endpoints.health.jikkou.enabled",
            notEquals = "false"
    ), @Requires(
            beans = {HealthEndpoint.class}
    )})
    public List<JikkouHealthIndicator> jikkouHealthIndicators(JikkouApi api,
                                                              JikkouHealthIndicatorConfiguration configuration) {
        ApiHealthIndicatorList indicatorList = api.getApiHealthIndicators();
        return indicatorList.indicators()
                .stream()
                .map(indicator -> new JikkouHealthIndicator(api, indicator, configuration))
                .toList();
    }
}