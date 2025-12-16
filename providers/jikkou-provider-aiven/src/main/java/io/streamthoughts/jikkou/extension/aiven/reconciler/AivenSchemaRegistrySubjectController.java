/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.AivenAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.handler.CreateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.DeleteSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.UpdateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aiven - Schema Registry Subjects Controller.
 */
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(
        apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
        kind = ApiVersions.SCHEMA_REGISTRY_KIND
)
@SupportedResource(
        apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
        kind = ApiVersions.SCHEMA_REGISTRY_CHANGE_KIND
)
public class AivenSchemaRegistrySubjectController implements Controller<V1SchemaRegistrySubject> {

    private static final Logger LOG = LoggerFactory.getLogger(AivenSchemaRegistrySubjectController.class);

    private AivenApiClientConfig apiClientConfig;
    private CompatibilityLevels globalCompatibilityLevel = null;

    /**
     * Creates a new {@link AivenSchemaRegistrySubjectController} instance.
     */
    public AivenSchemaRegistrySubjectController() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistrySubjectCollector} instance.
     *
     * @param config the configuration.
     */
    public AivenSchemaRegistrySubjectController(final AivenApiClientConfig config) {
        this.apiClientConfig = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.apiClientConfig = context.<AivenExtensionProvider>provider().apiClientConfig();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {
        AsyncSchemaRegistryApi api = new AivenAsyncSchemaRegistryApi(AivenApiClientFactory.create(apiClientConfig));
        try {
            List<ChangeHandler> handlers = List.of(
                new CreateSchemaSubjectChangeHandler(api),
                new UpdateSchemaSubjectChangeHandler(api),
                new DeleteSchemaSubjectChangeHandler(api),
                new ChangeHandler.None(SchemaSubjectChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        } finally {
            api.close();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1SchemaRegistrySubject> resources,
            @NotNull ReconciliationContext context) {

        // Get described resources that are candidates for this reconciliation.
        List<V1SchemaRegistrySubject> expectedSubjects = resources.stream()
                .filter(context.selector()::apply)
                .map(this::useGlobalCompatibilityLevelIfUnspecified)
                .toList();

        // Get existing resources from the environment.
        AivenSchemaRegistrySubjectCollector collector = new AivenSchemaRegistrySubjectCollector(apiClientConfig)
                .prettyPrintSchema(false);

        List<V1SchemaRegistrySubject> actualSubjects = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
                .filter(context.selector()::apply)
                .toList();

        SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

        // Compute changes
        return computer.computeChanges(actualSubjects, expectedSubjects)
                .stream()
                .map(change -> GenericResourceChange
                        .builder()
                        .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1)
                        .withKind(ApiVersions.SCHEMA_REGISTRY_CHANGE_KIND)
                        .withMetadata(change.getMetadata())
                        .withSpec((GenericResourceChangeSpec) change.getSpec())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @NotNull
    private V1SchemaRegistrySubject useGlobalCompatibilityLevelIfUnspecified(final @NotNull V1SchemaRegistrySubject item) {
        CompatibilityLevels compatibilityLevel = item.getSpec().getCompatibilityLevel();
        if (compatibilityLevel == null) {
            return item.withSpec(
                    item.getSpec()
                            .withCompatibilityLevel(getGlobalCompatibilityLevel())
            );
        }
        return item;
    }

    @Nullable
    private CompatibilityLevels getGlobalCompatibilityLevel() {
        if (globalCompatibilityLevel == null) {
            try (AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);) {
                globalCompatibilityLevel = api.getSchemaRegistryGlobalCompatibility().compatibilityLevel();
            } catch (Exception e) {
                LOG.error("Failed to get to Schema Registry global compatibility level.", e);
            }
        }
        return globalCompatibilityLevel;
    }
}
