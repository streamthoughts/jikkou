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
package io.streamthoughts.jikkou.schema.registry.control;

import static io.streamthoughts.jikkou.core.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.change.ChangeExecutor;
import io.streamthoughts.jikkou.core.change.ChangeHandler;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.resource.BaseResourceController;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.handler.CreateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.DeleteSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.UpdateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChangeList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@AcceptsReconciliationModes(value = {CREATE, DELETE, UPDATE, APPLY_ALL})
@AcceptsResource(type = V1SchemaRegistrySubject.class)
public class SchemaRegistrySubjectController implements BaseResourceController<V1SchemaRegistrySubject, SchemaSubjectChange> {

    private SchemaRegistryClientConfig configuration;

    /**
     * Creates a new {@link SchemaRegistrySubjectController} instance.
     */
    public SchemaRegistrySubjectController() {
    }

    /**
     * Creates a new {@link SchemaRegistrySubjectController} instance.
     *
     * @param configuration the schema registry client configuration.
     */
    public SchemaRegistrySubjectController(@NotNull SchemaRegistryClientConfig configuration) {
        configure(configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new SchemaRegistryClientConfig(config));
    }

    private void configure(@NotNull SchemaRegistryClientConfig config) throws ConfigException {
        this.configuration = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<SchemaSubjectChange>> execute(@NotNull List<HasMetadataChange<SchemaSubjectChange>> items,
                                                           @NotNull ReconciliationMode mode,
                                                           boolean dryRun) {
        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(configuration))) {
            List<ChangeHandler<SchemaSubjectChange>> handlers = List.of(
                    new CreateSchemaSubjectChangeHandler(api),
                    new UpdateSchemaSubjectChangeHandler(api),
                    new DeleteSchemaSubjectChangeHandler(api),
                    new ChangeHandler.None<>(SchemaSubjectChangeDescription::new)
            );
            return new ChangeExecutor<>(handlers).execute(items, dryRun);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<? extends HasMetadataChange<SchemaSubjectChange>> computeReconciliationChanges(
            @NotNull Collection<V1SchemaRegistrySubject> resources,
            @NotNull ReconciliationMode mode,
            @NotNull ReconciliationContext context) {

        // Get described resources that are candidates for this reconciliation.
        List<V1SchemaRegistrySubject> expectedSubjects = resources.stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Get existing resources from the environment.
        SchemaRegistrySubjectCollector collector = new SchemaRegistrySubjectCollector(configuration)
                .prettyPrintSchema(false)
                .defaultToGlobalCompatibilityLevel(false);

        List<V1SchemaRegistrySubject> actualSubjects = collector.listAll(context.configuration()).stream()
                .filter(new AggregateSelector(context.selectors())::apply)
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
}
