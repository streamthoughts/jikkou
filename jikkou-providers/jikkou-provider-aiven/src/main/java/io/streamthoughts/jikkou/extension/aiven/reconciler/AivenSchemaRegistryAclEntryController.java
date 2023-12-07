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
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.change.schema.SchemaRegistryAclEntryChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.change.schema.SchemaRegistryAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, FULL}
)
@SupportedResource(type = V1SchemaRegistryAclEntry.class)
public final class AivenSchemaRegistryAclEntryController implements Controller<V1SchemaRegistryAclEntry, ResourceChange> {

    public static final ConfigProperty<Boolean> DELETE_ORPHANS_OPTIONS = ConfigProperty
            .ofBoolean("delete-orphans")
            .orElse(false);

    private AivenApiClientConfig config;
    private AivenSchemaRegistryAclEntryCollector collector;

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryController} instance.
     */
    public AivenSchemaRegistryAclEntryController() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryController} instance.
     *
     * @param config the schema registry client configuration.
     */
    public AivenSchemaRegistryAclEntryController(@NotNull AivenApiClientConfig config) {
        init(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(new AivenApiClientConfig(context.appConfiguration()));
    }

    private void init(@NotNull AivenApiClientConfig config) {
        this.config = config;
        this.collector = new AivenSchemaRegistryAclEntryCollector(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor<ResourceChange> executor,
                                      @NotNull ReconciliationContext context) {

        AivenApiClient api = AivenApiClientFactory.create(config);
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

        Boolean deleteOrphans = DELETE_ORPHANS_OPTIONS.get(context.configuration());
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(deleteOrphans);

        return computer.computeChanges(actualResources, expectedResources);
    }
}
