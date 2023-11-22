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
package io.streamthoughts.jikkou.rest.beans;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.selectors.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.rest.configs.security.SecurityConfiguration;
import io.streamthoughts.jikkou.rest.health.indicator.JikkouHealthIndicator;
import io.streamthoughts.jikkou.rest.health.indicator.JikkouHealthIndicatorConfiguration;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import io.streamthoughts.jikkou.runtime.configurator.ChangeReporterApiConfigurator;
import io.streamthoughts.jikkou.runtime.configurator.TransformationApiConfigurator;
import io.streamthoughts.jikkou.runtime.configurator.ValidationApiConfigurator;
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
    public ExtensionFactory extensionFactory(Configuration configuration,
                                             ExtensionDescriptorRegistry registry) {
        return new DefaultExtensionFactory(registry, configuration);
    }

    @Singleton
    public JikkouContext jikkouContext(Configuration configuration,
                                       ExtensionFactory factory,
                                       ResourceRegistry resourceRegistry) {
        return new JikkouContext(configuration, factory, resourceRegistry);
    }

    @Singleton
    public ResourceRegistry resourceRegistry() {
        // manually register core resources
        DefaultResourceRegistry registry = new DefaultResourceRegistry();
        registry.register(ApiExtensionList.class);
        registry.register(ApiHealthIndicatorList.class);
        return registry;
    }

    @Singleton
    public JikkouApi jikkouApi(JikkouContext context, ExtensionDescriptorRegistry registry) {
        ApiConfigurator[] configurators = {
                new ValidationApiConfigurator(registry),
                new TransformationApiConfigurator(registry),
                new ChangeReporterApiConfigurator(registry)
        };
        return context.createApi(configurators);
    }

    @Singleton
    public ExpressionSelectorFactory expressionResourceSelectorFactory() {
        return new ExpressionSelectorFactory();
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
                .map(indicator -> new JikkouHealthIndicator(api, indicator, configuration) )
                .toList();
    }
}