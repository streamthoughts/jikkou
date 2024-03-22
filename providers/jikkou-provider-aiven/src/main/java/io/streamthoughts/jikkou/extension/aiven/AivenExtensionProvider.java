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
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
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
import io.streamthoughts.jikkou.spi.AbstractExtensionProvider;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

@Named("aiven")
public final class AivenExtensionProvider extends AbstractExtensionProvider {

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
        Stream.of(V1KafkaTopicAclEntry.class,
            V1KafkaTopicAclEntryList.class,
            V1SchemaRegistryAclEntry.class,
            V1SchemaRegistryAclEntryList.class,
            V1KafkaQuota.class,
            V1KafkaQuotaList.class
        ).forEach(cls -> registerResource(registry, cls));

        registry.register(V1SchemaRegistrySubject.class, KAFKA_AIVEN_V1BETA1)
            .setSingularName("avn-schemaregistrysubject")
            .setPluralName("avn-schemaregistrysubjects")
            .setShortNames(null);

        registry.register(GenericResourceChange.class, ResourceType.of(
            ResourceChange.getChangeKindFromResource(V1SchemaRegistrySubject.class),
            KAFKA_AIVEN_V1BETA1
        ));

        registry.register(V1KafkaTopic.class, KAFKA_AIVEN_V1BETA2)
            .setSingularName("avn-kafkatopic")
            .setPluralName("avn-kafkatopics")
            .setShortNames(Set.of("avn-kt"));

        registry.register(GenericResourceChange.class, ResourceType.of(
            ResourceChange.getChangeKindFromResource(V1KafkaTopic.class),
            KAFKA_AIVEN_V1BETA2
        ));

    }
}
