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
package io.streamthoughts.jikkou.extension.aiven.control;

import static io.streamthoughts.jikkou.api.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.api.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.DELETE;

import io.streamthoughts.jikkou.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.ChangeExecutor;
import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseResourceController;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import io.streamthoughts.jikkou.extension.aiven.change.SchemaRegistryAclEntryChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.change.handler.CreateSchemaRegistryAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.handler.DeleteSchemaRegistryAclEntryChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.converter.V1SchemaRegistryAclEntryListConverter;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@AcceptsReconciliationModes(value = {CREATE, DELETE, APPLY_ALL})
@AcceptsResource(type = V1SchemaRegistryAclEntry.class)
@AcceptsResource(type = V1SchemaRegistryAclEntry.class, converter = V1SchemaRegistryAclEntryListConverter.class)
public final class AivenSchemaRegistryAclEntryController implements BaseResourceController<V1SchemaRegistryAclEntry, ValueChange<SchemaRegistryAclEntry>> {

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
        configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new AivenApiClientConfig(config));
    }

    private void configure(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.config = config;
        this.collector = new AivenSchemaRegistryAclEntryCollector(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> execute(@NotNull List<HasMetadataChange<ValueChange<SchemaRegistryAclEntry>>> items,
                                                                           @NotNull ReconciliationMode mode,
                                                                           boolean dryRun) {

        AivenApiClient api = AivenApiClientFactory.create(config);
        try {
            List<ChangeHandler<ValueChange<SchemaRegistryAclEntry>>> handlers = List.of(
                    new CreateSchemaRegistryAclEntryChangeHandler(api),
                    new DeleteSchemaRegistryAclEntryChangeHandler(api),
                    new ChangeHandler.None<>(it -> KafkaChangeDescriptions.of(
                            it.getChange().getChangeType(),
                            it.getChange().getAfter()))
            );
            return new ChangeExecutor<>(handlers).execute(items, dryRun);
        } finally {
            api.close();
        }

    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<? extends HasMetadataChange<ValueChange<SchemaRegistryAclEntry>>> computeReconciliationChanges(
            @NotNull Collection<V1SchemaRegistryAclEntry> resources,
            @NotNull ReconciliationMode mode,
            @NotNull ReconciliationContext context) {

        // Get existing resources from the environment.
        List<V1SchemaRegistryAclEntry> actualResources = collector.listAll(context.configuration()).stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Get expected resources which are candidates for this reconciliation.
        List<V1SchemaRegistryAclEntry> expectedResources = resources.stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        Boolean deleteOrphans = DELETE_ORPHANS_OPTIONS.evaluate(context.configuration());
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(deleteOrphans);

        List<HasMetadataChange<ValueChange<SchemaRegistryAclEntry>>> changes =
                computer.computeChanges(actualResources, expectedResources);

        return GenericResourceListObject
                .<HasMetadataChange<ValueChange<SchemaRegistryAclEntry>>>builder()
                .withItems(changes)
                .build();
    }
}
