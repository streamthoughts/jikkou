/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven;

import static io.streamthoughts.jikkou.extension.aiven.ApiVersions.KAFKA_AIVEN_V1BETA1;
import static io.streamthoughts.jikkou.extension.aiven.ApiVersions.KAFKA_AIVEN_V1BETA2;

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.health.AivenServiceHealthIndicator;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaQuotaCollector;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaQuotaController;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaTopicAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaTopicAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaTopicCollector;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenKafkaTopicController;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenSchemaRegistryAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenSchemaRegistryAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenSchemaRegistrySubjectCollector;
import io.streamthoughts.jikkou.extension.aiven.reconciler.AivenSchemaRegistrySubjectController;
import io.streamthoughts.jikkou.extension.aiven.validation.AivenSchemaCompatibilityValidation;
import io.streamthoughts.jikkou.extension.aiven.validation.SchemaRegistryAclEntryValidation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.spi.BaseExtensionProvider;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

@Named("aiven")
@Provider(
    name = "aiven",
    description = "Extension provider for Aiven",
    tags = {"Aiven", "Apache Kafka", "Cloud"}
)
public final class AivenExtensionProvider extends BaseExtensionProvider {

    final ConfigProperty<String> project = ConfigProperty
        .ofString("project")
        .description("Aiven project name.");

    final ConfigProperty<String> service = ConfigProperty
        .ofString("service")
        .description("Aiven Service name.");

    final ConfigProperty<String> apiUrl = ConfigProperty
        .ofString("apiUrl")
        .orElse("https://api.aiven.io/v1/")
        .description("URL to the Aiven REST API.");

    final ConfigProperty<String> tokenAuth = ConfigProperty
        .ofString("tokenAuth")
        .description("Aiven Bearer Token. Tokens can be obtained from your Aiven profile page");

    final ConfigProperty<Boolean> debugLoggingEnabled = ConfigProperty
        .ofBoolean("debugLoggingEnabled")
        .description("Enable debug logging.")
        .orElse(false);

    private AivenApiClientConfig apiClientConfig;

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        apiClientConfig = new AivenApiClientConfig(
            apiUrl.get(configuration),
            tokenAuth.get(configuration),
            project.get(configuration),
            service.get(configuration),
            debugLoggingEnabled.get(configuration)
        );
    }

    public AivenApiClientConfig apiClientConfig() {
        return apiClientConfig;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(AivenServiceHealthIndicator.class, AivenServiceHealthIndicator::new);
        registry.register(AivenKafkaTopicAclEntryCollector.class, AivenKafkaTopicAclEntryCollector::new);
        registry.register(AivenKafkaTopicAclEntryController.class, AivenKafkaTopicAclEntryController::new);
        registry.register(AivenSchemaRegistryAclEntryCollector.class, AivenSchemaRegistryAclEntryCollector::new);
        registry.register(AivenSchemaRegistryAclEntryController.class, AivenSchemaRegistryAclEntryController::new);
        registry.register(AivenSchemaRegistrySubjectCollector.class, AivenSchemaRegistrySubjectCollector::new);
        registry.register(AivenSchemaRegistrySubjectController.class, AivenSchemaRegistrySubjectController::new);
        registry.register(SchemaRegistryAclEntryValidation.class, SchemaRegistryAclEntryValidation::new);
        registry.register(AivenKafkaQuotaCollector.class, AivenKafkaQuotaCollector::new);
        registry.register(AivenKafkaQuotaController.class, AivenKafkaQuotaController::new);
        registry.register(AivenSchemaCompatibilityValidation.class, AivenSchemaCompatibilityValidation::new);
        registry.register(AivenKafkaTopicController.class, AivenKafkaTopicController::new);
        registry.register(AivenKafkaTopicCollector.class, AivenKafkaTopicCollector::new);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1KafkaTopicAclEntry.class);
        registry.register(V1KafkaTopicAclEntryList.class);
        registry.register(V1SchemaRegistryAclEntry.class);
        registry.register(V1SchemaRegistryAclEntryList.class);
        registry.register(V1KafkaQuota.class);
        registry.register(V1KafkaQuotaList.class);

        registry.register(V1SchemaRegistrySubject.class, KAFKA_AIVEN_V1BETA1)
            .setSingularName("avn-schemaregistrysubject")
            .setPluralName("avn-schemaregistrysubjects")
            .setShortNames(Set.of("avnsr"));

        registry.register(GenericResourceChange.class, ResourceType.of(
            ResourceChange.getChangeKindFromResource(V1SchemaRegistrySubject.class),
            KAFKA_AIVEN_V1BETA1
        ));

        registry.register(V1KafkaTopic.class, KAFKA_AIVEN_V1BETA2)
            .setSingularName("avn-kafkatopic")
            .setPluralName("avn-kafkatopics")
            .setShortNames(Set.of("avnkt"));

        registry.register(GenericResourceChange.class, ResourceType.of(
            ResourceChange.getChangeKindFromResource(V1KafkaTopic.class),
            KAFKA_AIVEN_V1BETA2
        ));

    }
}
