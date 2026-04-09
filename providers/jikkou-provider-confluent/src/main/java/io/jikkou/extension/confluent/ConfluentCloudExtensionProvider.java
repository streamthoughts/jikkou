/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent;

import io.jikkou.core.annotation.Named;
import io.jikkou.core.annotation.Provider;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionRegistry;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientConfig;
import io.jikkou.extension.confluent.collections.V1RoleBindingList;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import io.jikkou.extension.confluent.reconciler.ConfluentCloudRoleBindingCollector;
import io.jikkou.extension.confluent.reconciler.ConfluentCloudRoleBindingController;
import io.jikkou.spi.BaseExtensionProvider;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Named("confluent-cloud")
@Provider(
    name = "confluent-cloud",
    description = "Extension provider for Confluent Cloud",
    tags = {"Confluent Cloud", "Apache Kafka", "Cloud", "RBAC"}
)
public final class ConfluentCloudExtensionProvider extends BaseExtensionProvider {

    interface Config {
        ConfigProperty<String> API_URL = ConfigProperty
            .ofString("apiUrl")
            .displayName("API URL")
            .defaultValue("https://api.confluent.cloud")
            .description("URL to the Confluent Cloud REST API.");

        ConfigProperty<String> API_KEY = ConfigProperty
            .ofString("apiKey")
            .displayName("API Key")
            .description("Confluent Cloud API Key.");

        ConfigProperty<String> API_SECRET = ConfigProperty
            .ofString("apiSecret")
            .displayName("API Secret")
            .description("Confluent Cloud API Secret.");

        ConfigProperty<String> CRN_PATTERN = ConfigProperty
            .ofString("crnPattern")
            .displayName("CRN Pattern")
            .description("CRN pattern used to scope role binding list operations.");

        ConfigProperty<Boolean> DEBUG_LOGGING_ENABLED = ConfigProperty
            .ofBoolean("debugLoggingEnabled")
            .displayName("Debug Logging")
            .description("Enable debug logging.")
            .defaultValue(false);
    }

    private ConfluentCloudApiClientConfig apiClientConfig;

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        apiClientConfig = new ConfluentCloudApiClientConfig(
            Config.API_URL.get(configuration),
            Config.API_KEY.get(configuration),
            Config.API_SECRET.get(configuration),
            Config.CRN_PATTERN.get(configuration),
            Config.DEBUG_LOGGING_ENABLED.get(configuration)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.API_URL,
            Config.API_KEY,
            Config.API_SECRET,
            Config.CRN_PATTERN,
            Config.DEBUG_LOGGING_ENABLED
        );
    }

    public ConfluentCloudApiClientConfig apiClientConfig() {
        return apiClientConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(ConfluentCloudRoleBindingCollector.class, ConfluentCloudRoleBindingCollector::new);
        registry.register(ConfluentCloudRoleBindingController.class, ConfluentCloudRoleBindingController::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1RoleBinding.class);
        registry.register(V1RoleBindingList.class);
    }
}
