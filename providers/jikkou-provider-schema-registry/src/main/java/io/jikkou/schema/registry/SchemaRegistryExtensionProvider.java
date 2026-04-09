/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry;

import io.jikkou.common.utils.Enums;
import io.jikkou.core.annotation.Provider;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionRegistry;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.http.client.ssl.SSLConfig;
import io.jikkou.schema.registry.api.AuthMethod;
import io.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.jikkou.schema.registry.health.SchemaRegistryHealthIndicator;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.jikkou.schema.registry.reconciler.SchemaRegistrySubjectCollector;
import io.jikkou.schema.registry.reconciler.SchemaRegistrySubjectController;
import io.jikkou.schema.registry.transform.NormalizeSubjectSchemaTransformation;
import io.jikkou.schema.registry.validation.AvroSchemaValidation;
import io.jikkou.schema.registry.validation.CompatibilityLevelValidation;
import io.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import io.jikkou.schema.registry.validation.SubjectNameRegexValidation;
import io.jikkou.spi.BaseExtensionProvider;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Schema Registry.
 */
@Provider(
    name = "schemaregistry",
    description = "Extension provider for Schema Registry",
    tags = {"Apache Kafka", "Schema Registry"}
)
public final class SchemaRegistryExtensionProvider extends BaseExtensionProvider {

    public interface Config {
        ConfigProperty<String> SCHEMA_REGISTRY_URL = ConfigProperty
            .ofString("url")
            .displayName("URL")
            .required(true)
            .description("Comma-separated list of URLs for schema registry instances that can be used to register or look up schemas.");

        ConfigProperty<String> SCHEMA_REGISTRY_VENDOR = ConfigProperty
            .ofString("vendor")
            .displayName("Vendor")
            .defaultValue("generic")
            .description("The name of the schema registry implementation vendor.");

        ConfigProperty<AuthMethod> SCHEMA_REGISTRY_AUTH_METHOD = ConfigProperty
            .ofString("authMethod")
            .displayName("Auth Method")
            .map(val -> Enums.getForNameIgnoreCase(val, AuthMethod.class, AuthMethod.INVALID))
            .defaultValue(AuthMethod.NONE)
            .description("Method to use for authenticating on Schema Registry. Available values are: [none, basicauth, ssl]");

        ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_USER = ConfigProperty
            .ofString("basicAuthUser")
            .displayName("Basic Auth Username")
            .description("Use when 'authMethod' is 'basicauth' to specify the username for Authorization Basic header");

        ConfigProperty<String> SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD = ConfigProperty
            .ofString("basicAuthPassword")
            .displayName("Basic Auth Password")
            .description("Use when 'authMethod' is 'basicauth' to specify the password for Authorization Basic header");

        ConfigProperty<Boolean> SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED = ConfigProperty
            .ofBoolean("debugLoggingEnabled")
            .displayName("Debug Logging")
            .description("Enable debug logging.")
            .defaultValue(false);

        ConfigProperty<Boolean> NORMALIZE_SCHEMAS_ENABLED = ConfigProperty
            .ofBoolean("normalizeSchemasEnabled")
            .displayName("Normalize Schemas")
            .description("Specify whether to normalize schemas (default: true).")
            .defaultValue(true);
    }

    private SchemaRegistryClientConfig clientConfig;

    /** {@inheritDoc} **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.SCHEMA_REGISTRY_URL,
            Config.SCHEMA_REGISTRY_VENDOR,
            Config.SCHEMA_REGISTRY_AUTH_METHOD,
            Config.SCHEMA_REGISTRY_BASIC_AUTH_USER,
            Config.SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD,
            Config.SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED,
            Config.NORMALIZE_SCHEMAS_ENABLED
        );
    }

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        List<String> urls = Arrays.stream(Config.SCHEMA_REGISTRY_URL.get(configuration).split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        this.clientConfig = new SchemaRegistryClientConfig(
            urls,
            Config.SCHEMA_REGISTRY_VENDOR.get(configuration),
            Config.SCHEMA_REGISTRY_AUTH_METHOD.get(configuration),
            () -> Config.SCHEMA_REGISTRY_BASIC_AUTH_USER.get(configuration),
            () -> Config.SCHEMA_REGISTRY_BASIC_AUTH_PASSWORD.get(configuration),
            () -> SSLConfig.from(configuration),
            Config.SCHEMA_REGISTRY_DEBUG_LOGGING_ENABLED.get(configuration)
        );
    }

    public SchemaRegistryClientConfig clientConfig() {
        return clientConfig;
    }

    public boolean isNormalizeSchemaEnabled() {
        return Config.NORMALIZE_SCHEMAS_ENABLED.get(configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        // Collectors
        registry.register(SchemaRegistrySubjectCollector.class, SchemaRegistrySubjectCollector::new);

        // Controllers
        registry.register(SchemaRegistrySubjectController.class, SchemaRegistrySubjectController::new);

        // Validations
        registry.register(AvroSchemaValidation.class, AvroSchemaValidation::new);
        registry.register(SchemaCompatibilityValidation.class, SchemaCompatibilityValidation::new);
        registry.register(CompatibilityLevelValidation.class, CompatibilityLevelValidation::new);
        registry.register(SubjectNameRegexValidation.class, SubjectNameRegexValidation::new);

        // Transformations
        registry.register(NormalizeSubjectSchemaTransformation.class, NormalizeSubjectSchemaTransformation::new);

        // Health indicators
        registry.register(SchemaRegistryHealthIndicator.class, SchemaRegistryHealthIndicator::new);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1SchemaRegistrySubject.class).setReconciliationOrder(100);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1SchemaRegistrySubject.class));
        registry.register(V1SchemaRegistrySubjectList.class);
    }
}
