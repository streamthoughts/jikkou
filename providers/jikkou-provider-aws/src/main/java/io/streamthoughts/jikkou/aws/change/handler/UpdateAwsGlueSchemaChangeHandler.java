/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change.handler;

import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_COMPATIBILITY;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_SCHEMA;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_SCHEMA_DESCRIPTION;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.aws.AwsGlueLabelsAndAnnotations;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.Compatibility;
import software.amazon.awssdk.services.glue.model.GetSchemaVersionRequest;
import software.amazon.awssdk.services.glue.model.RegisterSchemaVersionRequest;
import software.amazon.awssdk.services.glue.model.RegisterSchemaVersionResponse;
import software.amazon.awssdk.services.glue.model.SchemaId;
import software.amazon.awssdk.services.glue.model.SchemaVersionNumber;
import software.amazon.awssdk.services.glue.model.SchemaVersionStatus;
import software.amazon.awssdk.services.glue.model.UpdateSchemaRequest;

public final class UpdateAwsGlueSchemaChangeHandler
    extends AbstractAwsGlueSchemaChangeHandler
    implements ChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAwsGlueSchemaChangeHandler.class);

    public static final SchemaVersionNumber LATEST_VERSION_NUMBER = SchemaVersionNumber
        .builder()
        .latestVersion(true)
        .build();

    /**
     * Creates a new {@link UpdateAwsGlueSchemaChangeHandler} instance.
     *
     * @param client the {@link GlueClient} instance.
     */
    public UpdateAwsGlueSchemaChangeHandler(@NotNull final GlueClient client) {
        super(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return Set.of(UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {

        List<ChangeResponse> results = new ArrayList<>();
        for (ResourceChange change : changes) {
            Mono<Void> mono = Mono.empty();

            SpecificStateChange<String> schemaDefinition = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_SCHEMA, TypeConverter.String());

            final String schemaName = change.getMetadata().getName();
            final String registryName = (String) change.getMetadata()
                .getLabelByKey(AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_NAME)
                .getValue();

            // Schema Definition
            if (schemaDefinition.getOp().isUpdateOrCreate()) {
                mono = updateSchema(registryName, schemaName, schemaDefinition);
            }

            // Compatibility
            Optional<SpecificStateChange<Compatibility>> compatibility = StateChangeList
                .of(change.getSpec().getChanges())
                .findLast(DATA_COMPATIBILITY, TypeConverter.of(Compatibility.class))
                .filter(it -> it.getOp().isUpdateOrCreate());

            if (compatibility.isPresent()) {
                mono = mono.then(updateCompatibility(registryName, schemaName, compatibility.get()));
            }

            // Description
            Optional<SpecificStateChange<String>> description = StateChangeList
                .of(change.getSpec().getChanges())
                .findLast(DATA_SCHEMA_DESCRIPTION, TypeConverter.String())
                .filter(it -> it.getOp().isUpdateOrCreate());

            if (description.isPresent()) {
                mono = mono.then(updateDescription(registryName, schemaName, description.get()));
            }

            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }

    private Mono<Void> updateSchema(final String registryName,
                                    final String schemaName,
                                    final SpecificStateChange<String> schemaDefinition) {
        final SchemaId schemaId = SchemaId.builder()
            .registryName(registryName)
            .schemaName(schemaName)
            .build();

        final RegisterSchemaVersionRequest request = RegisterSchemaVersionRequest.builder()
            .schemaId(schemaId)
            .schemaDefinition(schemaDefinition.getAfter())
            .build();

        return Mono.fromRunnable(() -> {
            LOG.debug("Updating schema version (schemaName='{}', registryName='{}').", registryName, schemaName);
            RegisterSchemaVersionResponse response = client.registerSchemaVersion(request);
            SchemaVersionStatus status = response.status();
            String schemaVersionId = response.schemaVersionId();
            while (status.equals(SchemaVersionStatus.PENDING)) {
                LOG.debug("Waiting for schema version status (registryName='{}', schemaName='{}', schemaVersionId='{}',status='{}').", registryName, schemaName, schemaVersionId, status);
                status = client.getSchemaVersion(GetSchemaVersionRequest.builder().schemaVersionId(schemaVersionId).build()).status();
                try {
                    Thread.sleep(Duration.ofMillis(200));
                } catch (InterruptedException e) {
                    throw new io.streamthoughts.jikkou.core.exceptions.InterruptedException(e);
                }
            }

            if (status == SchemaVersionStatus.FAILURE) {
                throw new RuntimeException("Unable to update schema version (registryName='%s', schemaName='%s', schemaVersionId='%s',status='%s').".formatted(registryName, schemaName, schemaVersionId, status));
            } else {
                LOG.debug("Schema version updated (registryName='{}', schemaName='{}', schemaVersionId='{}',status='{}').", registryName, schemaName, schemaVersionId, status);
            }
        });
    }

    private Mono<Void> updateDescription(final String registryName,
                                         final String schemaName,
                                         final SpecificStateChange<String> description) {
        final UpdateSchemaRequest request = UpdateSchemaRequest.builder()
            .schemaId(SchemaId.builder()
                .registryName(registryName)
                .schemaName(schemaName)
                .build())
            .description(description.getAfter())
            .schemaVersionNumber(LATEST_VERSION_NUMBER)
            .build();
        return Mono.fromRunnable(() -> client.updateSchema(request));
    }

    private Mono<Void> updateCompatibility(final String registryName,
                                           final String schemaName,
                                           final SpecificStateChange<Compatibility> compatibility) {
        final UpdateSchemaRequest request = UpdateSchemaRequest.builder()
            .schemaId(SchemaId.builder()
                .registryName(registryName)
                .schemaName(schemaName)
                .build())
            .compatibility(compatibility.getAfter()) // Options: NONE, BACKWARD, FORWARD, FULL
            .schemaVersionNumber(LATEST_VERSION_NUMBER)
            .build();
        return Mono.fromRunnable(() -> client.updateSchema(request));
    }
}
