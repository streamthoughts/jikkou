/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.aws.ApiVersions;
import io.streamthoughts.jikkou.aws.AwsExtensionProvider;
import io.streamthoughts.jikkou.aws.AwsGlueAnnotations;
import io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer;
import io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeDescription;
import io.streamthoughts.jikkou.aws.change.handler.CreateAwsGlueSchemaChangeHandler;
import io.streamthoughts.jikkou.aws.change.handler.DeleteAwsGlueSchemaChangeHandler;
import io.streamthoughts.jikkou.aws.change.handler.UpdateAwsGlueSchemaChangeHandler;
import io.streamthoughts.jikkou.aws.models.AwsGlueSchema;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.GlueClient;

@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = AwsGlueSchema.class)
@SupportedResource(apiVersion = ApiVersions.AWS_GLUE_API_VERSION, kind = "AwsGlueSchemaChange")
public class AwsGlueSchemaController
        extends ContextualExtension
        implements Controller<AwsGlueSchema, ResourceChange> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull final ReconciliationContext context) {

        final AwsExtensionProvider provider = extensionContext().provider();
        try (GlueClient client = provider.newGlueClient()) {
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                new CreateAwsGlueSchemaChangeHandler(client),
                new UpdateAwsGlueSchemaChangeHandler(client),
                new DeleteAwsGlueSchemaChangeHandler(client),
                new ChangeHandler.None<>(AwsGlueSchemaChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<AwsGlueSchema> resources,
            @NotNull ReconciliationContext context) {

        // Get described resources that are candidates for this reconciliation.
        Map<String, List<AwsGlueSchema>> expectedSchemas = groupBySchemaRegistryName(resources, context.selector()::apply);


        // Get existing resources from the environment.
        AwsGlueSchemaCollector collector = new AwsGlueSchemaCollector();
        collector.init(this.extensionContext().contextForExtension(AwsGlueSchemaCollector.class));

        AwsGlueSchemaChangeComputer computer = new AwsGlueSchemaChangeComputer();
        List<ResourceChange> allChanges = new LinkedList<>();
        for (Map.Entry<String, List<AwsGlueSchema>> schemas : expectedSchemas.entrySet()) {
            Set<String> registryNames = schemas.getValue().stream()
                .map(AwsGlueSchema::getMetadata)
                .map(it -> it.findAnnotationByKey(AwsGlueAnnotations.SCHEMA_REGISTRY_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .collect(Collectors.toSet());

            List<AwsGlueSchema> actualSubjects = collector.listAll(registryNames, context.selector()).stream()
                .filter(context.selector()::apply)
                .toList();

            // Compute changes
            allChanges.addAll(computer.computeChanges(actualSubjects, schemas.getValue()));
        }

        return allChanges;
    }

    @NotNull
    private <T extends HasMetadata> Map<String, List<T>> groupBySchemaRegistryName(@NotNull Collection<T> changes,
                                                                                   @NotNull Predicate<T> predicate) {
        return changes
            .stream()
            .filter(predicate)
            .collect(Collectors.groupingBy(
                it -> it.getMetadata()
                    .getLabelByKey(AwsGlueAnnotations.SCHEMA_REGISTRY_NAME)
                    .getValue()
                    .toString(),
                Collectors.toList())
            );
    }
}
