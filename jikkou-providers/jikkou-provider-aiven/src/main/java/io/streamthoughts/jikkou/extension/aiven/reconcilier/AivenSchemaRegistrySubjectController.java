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
package io.streamthoughts.jikkou.extension.aiven.reconcilier;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selectors.Selectors;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.AivenAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.handler.CreateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.DeleteSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.UpdateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChangeList;
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
        apiVersion = AivenExtensionProvider.SCHEMA_REGISTRY_API_VERSION,
        kind = AivenExtensionProvider.SCHEMA_REGISTRY_KIND
)
public class AivenSchemaRegistrySubjectController implements Controller<V1SchemaRegistrySubject, SchemaSubjectChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AivenSchemaRegistrySubjectController.class);

    private AivenApiClientConfig configuration;

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
    public AivenSchemaRegistrySubjectController(AivenApiClientConfig config) {
        configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        configure(new AivenApiClientConfig(configuration));
    }

    private void configure(@NotNull AivenApiClientConfig configuration) throws ConfigException {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<SchemaSubjectChange>> execute(@NotNull ChangeExecutor<SchemaSubjectChange> executor,
                                                           @NotNull ReconciliationContext context) {
        AsyncSchemaRegistryApi api = new AivenAsyncSchemaRegistryApi(AivenApiClientFactory.create(configuration));
        try {
            List<ChangeHandler<SchemaSubjectChange>> handlers = List.of(
                    new CreateSchemaSubjectChangeHandler(api),
                    new UpdateSchemaSubjectChangeHandler(api),
                    new DeleteSchemaSubjectChangeHandler(api),
                    new ChangeHandler.None<>(SchemaSubjectChangeDescription::new)
            );
            return executor.execute(handlers);
        } finally {
            api.close();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<? extends HasMetadataChange<SchemaSubjectChange>> plan(
            @NotNull Collection<V1SchemaRegistrySubject> resources,
            @NotNull ReconciliationContext context) {
        LOG.info("Computing reconciliation ");
        // Get described resources that are candidates for this reconciliation.
        List<V1SchemaRegistrySubject> expectedSubjects = resources.stream()
                .filter(context.selector()::apply)
                .map(this::useGlobalCompatibilityLevelIfUnspecified)
                .toList();

        // Get existing resources from the environment.
        AivenSchemaRegistrySubjectCollector collector = new AivenSchemaRegistrySubjectCollector(configuration)
                .prettyPrintSchema(false);

        List<V1SchemaRegistrySubject> actualSubjects = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
                .filter(context.selector()::apply)
                .toList();

        SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

        // Compute changes
        List<V1SchemaRegistrySubjectChange> changes = computer.computeChanges(actualSubjects, expectedSubjects)
                .stream().map(it -> V1SchemaRegistrySubjectChange
                        .builder()
                        .withMetadata(it.getMetadata())
                        .withChange(it.getChange())
                        .build()
                )
                .collect(Collectors.toList());

        return V1SchemaRegistrySubjectChangeList.builder().withItems(changes).build();
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
            try (AivenApiClient api = AivenApiClientFactory.create(configuration);) {
                globalCompatibilityLevel = api.getSchemaRegistryGlobalCompatibility().compatibilityLevel();
            } catch (Exception e) {
                LOG.error("Failed to get to Schema Registry global compatibility level.", e);
            }
        }
        return globalCompatibilityLevel;
    }
}
