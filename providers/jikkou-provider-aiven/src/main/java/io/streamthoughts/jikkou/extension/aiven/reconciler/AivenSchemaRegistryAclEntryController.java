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

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
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
import io.streamthoughts.jikkou.extension.aiven.change.schema.SchemaRegistryAclEntryChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.change.schema.SchemaRegistryAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, FULL}
)
@SupportedResource(type = V1SchemaRegistryAclEntry.class)
@SupportedResource(
        apiVersion = ApiVersions.KAFKA_AIVEN_V1BETA1,
        kind = "SchemaRegistryAclEntryChange"
)
public final class AivenSchemaRegistryAclEntryController implements Controller<V1SchemaRegistryAclEntry, ResourceChange> {

    interface Config {
        ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .defaultValue(false);
    }

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private AivenApiClientConfig apiClientConfig;
    private AivenSchemaRegistryAclEntryCollector collector;

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryController} instance.
     */
    public AivenSchemaRegistryAclEntryController() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryController} instance.
     *
     * @param apiClientConfig the schema registry client configuration.
     */
    public AivenSchemaRegistryAclEntryController(@NotNull AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig apiClientConfig) {
        if (this.initialized.compareAndSet(false, true)) {
            this.apiClientConfig = apiClientConfig;
            this.collector = new AivenSchemaRegistryAclEntryCollector(apiClientConfig);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor<ResourceChange> executor,
                                      @NotNull ReconciliationContext context) {

        AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                    new SchemaRegistryAclEntryChangeHandler.Create(api),
                    new SchemaRegistryAclEntryChangeHandler.Delete(api),
                    new SchemaRegistryAclEntryChangeHandler.None()
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
            @NotNull Collection<V1SchemaRegistryAclEntry> resources,
            @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1SchemaRegistryAclEntry> actualResources = collector.listAll(context.configuration(), Selectors.NO_SELECTOR)
                .stream()
                .filter(context.selector()::apply)
                .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1SchemaRegistryAclEntry> expectedResources = resources
                .stream()
                .filter(context.selector()::apply)
                .toList();

        Boolean deleteOrphans = Config.DELETE_ORPHANS_OPTIONS.get(context.configuration());
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(deleteOrphans);

        return computer.computeChanges(actualResources, expectedResources);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(Config.DELETE_ORPHANS_OPTIONS);
    }
}
