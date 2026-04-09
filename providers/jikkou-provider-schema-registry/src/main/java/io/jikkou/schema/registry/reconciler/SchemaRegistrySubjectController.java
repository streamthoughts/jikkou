/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.reconciler;

import static io.jikkou.core.ReconciliationMode.*;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selector;
import io.jikkou.schema.registry.ApiVersions;
import io.jikkou.schema.registry.SchemaRegistryExtensionProvider;
import io.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.jikkou.schema.registry.api.DefaultAsyncSchemaRegistryApi;
import io.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.jikkou.schema.registry.change.SchemaSubjectChangeComputer;
import io.jikkou.schema.registry.change.SchemaSubjectChangeDescription;
import io.jikkou.schema.registry.change.handler.CreateSchemaSubjectChangeHandler;
import io.jikkou.schema.registry.change.handler.DeleteSchemaSubjectChangeHandler;
import io.jikkou.schema.registry.change.handler.UpdateSchemaSubjectChangeHandler;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Schema Registry subjects")
@Description("Reconciles Schema Registry subject resources to ensure they match the desired state.")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1SchemaRegistrySubject.class)
@SupportedResource(apiVersion = ApiVersions.SCHEMA_REGISTRY_V1BETA2, kind = "SchemaRegistrySubjectChange")
public class SchemaRegistrySubjectController
        extends ContextualExtension
        implements Controller<V1SchemaRegistrySubject> {

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
            configuration = context.<SchemaRegistryExtensionProvider>provider().clientConfig();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull final ReconciliationContext context) {
        try (AsyncSchemaRegistryApi api = new DefaultAsyncSchemaRegistryApi(SchemaRegistryApiFactory.create(configuration))) {
            List<ChangeHandler> handlers = List.of(
                    new CreateSchemaSubjectChangeHandler(api),
                    new UpdateSchemaSubjectChangeHandler(api),
                    new DeleteSchemaSubjectChangeHandler(api),
                    new ChangeHandler.None(SchemaSubjectChangeDescription::new)
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

        Selector selector = context.selector();

        // Get all expected resources (unfiltered)
        List<V1SchemaRegistrySubject> allExpectedSubjects = resources.stream().toList();

        // Get existing resources from the environment.
        SchemaRegistrySubjectCollector collector = new SchemaRegistrySubjectCollector(configuration)
            .prettyPrintSchema(false);

        List<String> subjects = allExpectedSubjects.stream()
            .map(V1SchemaRegistrySubject::getMetadata)
            .map(ObjectMeta::getName)
            .toList();

        Configuration collectorConfig = SchemaRegistrySubjectCollector.Config.DEFAULT_GLOBAL_COMPATIBILITY_LEVEL.asConfiguration(false);
        List<V1SchemaRegistrySubject> allActualSubjects = collector.listAll(collectorConfig, subjects).getItems();

        // Enrich actual subjects with labels from expected subjects so label selectors work on both sides
        Controller.enrichLabelsFromExpected(allActualSubjects, allExpectedSubjects);

        // Apply selector to both sides
        List<V1SchemaRegistrySubject> expectedSubjects = allExpectedSubjects.stream().filter(selector::apply).toList();
        List<V1SchemaRegistrySubject> actualSubjects = allActualSubjects.stream().filter(selector::apply).toList();

        SchemaSubjectChangeComputer computer = new SchemaSubjectChangeComputer();

        // Compute changes
        return computer.computeChanges(actualSubjects, expectedSubjects);
    }
}
