/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.http.client.ssl.SSLConfig;
import io.streamthoughts.jikkou.schema.registry.api.AuthMethod;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.health.SchemaRegistryHealthIndicator;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.reconciler.SchemaRegistrySubjectCollector;
import io.streamthoughts.jikkou.schema.registry.reconciler.SchemaRegistrySubjectController;
import io.streamthoughts.jikkou.schema.registry.transform.NormalizeSubjectSchemaTransformation;
import io.streamthoughts.jikkou.schema.registry.validation.AvroSchemaValidation;
import io.streamthoughts.jikkou.schema.registry.validation.CompatibilityLevelValidation;
import io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import io.streamthoughts.jikkou.spi.BaseExtensionProvider;
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

    final ConfigProperty<String> schemaRegistryUrl = ConfigProperty
        .ofString("url")
        .required(true)
        .description("Comma-separated list of URLs for schema registry instances that can be used to register or look up schemas.");

    final ConfigProperty<String> schemaRegistryVendor = ConfigProperty
        .ofString("vendor")
        .defaultValue("generic")
        .description("The name of the schema registry implementation vendor.");

    final ConfigProperty<AuthMethod> schemaRegistryAuthMethod = ConfigProperty
        .ofString("authMethod")
        .map(val -> Enums.getForNameIgnoreCase(val, AuthMethod.class, AuthMethod.INVALID))
        .defaultValue(AuthMethod.NONE)
        .description("Method to use for authenticating on Schema Registry. Available values are: [none, basicauth, ssl]");

    final ConfigProperty<String> schemaRegistryBasicAuthUser = ConfigProperty
        .ofString("basicAuthUser")
        .description("Use when 'authMethod' is 'basicauth' to specify the username for Authorization Basic header");

    final ConfigProperty<String> schemaRegistryBasicAuthPassword = ConfigProperty
        .ofString("basicAuthPassword")
        .description("Use when 'authMethod' is 'basicauth' to specify the password for Authorization Basic header");

    final ConfigProperty<Boolean> schemaRegistryDebugLoggingEnabled = ConfigProperty
        .ofBoolean("debugLoggingEnabled")
        .description("Enable debug logging.")
        .defaultValue(false);

    private SchemaRegistryClientConfig clientConfig;

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        this.clientConfig = new SchemaRegistryClientConfig(
            schemaRegistryUrl.get(configuration),
            schemaRegistryVendor.get(configuration),
            schemaRegistryAuthMethod.get(configuration),
            () -> schemaRegistryBasicAuthUser.get(configuration),
            () -> schemaRegistryBasicAuthPassword.get(configuration),
            () -> SSLConfig.from(configuration),
            schemaRegistryDebugLoggingEnabled.get(configuration)
        );
    }

    public SchemaRegistryClientConfig clientConfig() {
        return clientConfig;
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
        registry.register(V1SchemaRegistrySubject.class);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1SchemaRegistrySubject.class));
        registry.register(V1SchemaRegistrySubjectList.class);
    }
}
