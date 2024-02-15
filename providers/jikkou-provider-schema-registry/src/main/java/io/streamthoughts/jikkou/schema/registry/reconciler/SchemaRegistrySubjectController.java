/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.schema.registry.ApiVersions;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.streamthoughts.jikkou.schema.registry.change.handler.CreateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.DeleteSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.change.handler.UpdateSchemaSubjectChangeHandler;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1SchemaRegistrySubject.class)
@SupportedResource(apiVersion = ApiVersions.SCHEMA_REGISTRY_V1BETA2, kind = "SchemaRegistrySubjectChange")
public class SchemaRegistrySubjectController
    extends ContextualExtension
    implements Controller<V1SchemaRegistrySubject, ResourceChange> {

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
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (configuration == null) {
            configuration = new SchemaRegistryClientConfig(context.appConfiguration());
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull final ReconciliationContext context) {
        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(configuration))) {
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                new CreateSchemaSubjectChangeHandler(api),
                new UpdateSchemaSubjectChangeHandler(api),
                new DeleteSchemaSubjectChangeHandler(api),
                new ChangeHandler.None<>(SchemaSubjectChangeDescription::new)
            );
            return executor.applyChanges(handlers);
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
            .toList();

        // Get existing resources from the environment.
        SchemaRegistrySubjectCollector collector = new SchemaRegistrySubjectCollector(configuration)
            .prettyPrintSchema(false)
            .defaultToGlobalCompatibilityLevel(false);

        List<V1SchemaRegistrySubject> actualSubjects = collector.listAll(context.configuration(), Selectors.NO_SELECTOR).stream()
            .filter(context.selector()::apply)
            .toList();

        SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

        // Compute changes
        return computer.computeChanges(actualSubjects, expectedSubjects);
    }
}
