/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change.handler;

import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_COMPATIBILITY;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_FORMAT;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_SCHEMA;
import static io.streamthoughts.jikkou.aws.change.AwsGlueSchemaChangeComputer.DATA_SCHEMA_DESCRIPTION;

import io.streamthoughts.jikkou.aws.AwsGlueLabelsAndAnnotations;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.CreateSchemaRequest;
import software.amazon.awssdk.services.glue.model.DataFormat;
import software.amazon.awssdk.services.glue.model.RegistryId;

public final class CreateAwsGlueSchemaChangeHandler
    extends AbstractAwsGlueSchemaChangeHandler
    implements ChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAwsGlueSchemaChangeHandler.class);

    /**
     * Creates a new {@link CreateAwsGlueSchemaChangeHandler} instance.
     *
     * @param client the {@link GlueClient} instance.
     */
    public CreateAwsGlueSchemaChangeHandler(@NotNull GlueClient client) {
        super(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return Set.of(Operation.CREATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {

        List<ChangeResponse> results = new ArrayList<>();
        for (ResourceChange change : changes) {

            String schema = change.getSpec()
                .getChanges()
                .getLast(DATA_SCHEMA, TypeConverter.String())
                .getAfter();

            DataFormat dataFormat = change.getSpec()
                .getChanges()
                .getLast(DATA_FORMAT, TypeConverter.of(DataFormat.class))
                .getAfter();

            String registryName = (String) change.getMetadata()
                .getLabelByKey(AwsGlueLabelsAndAnnotations.SCHEMA_REGISTRY_NAME)
                .getValue();

            CreateSchemaRequest.Builder builder = CreateSchemaRequest.builder()
                .registryId(RegistryId.builder().registryName(registryName).build())
                .schemaName(change.getMetadata().getName())
                .dataFormat(dataFormat) // Supported: AVRO, JSON, PROTOBUF
                .schemaDefinition(schema);

            SpecificStateChange<String> compatibilityLevels = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_COMPATIBILITY, TypeConverter.String());

            if (compatibilityLevels != null) {
                builder = builder.compatibility(compatibilityLevels.getAfter());
            }

            SpecificStateChange<String> description = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_SCHEMA_DESCRIPTION, TypeConverter.String());

            if (description != null) {
                builder = builder.description(description.getAfter());
            }

            CreateSchemaRequest request = builder.build();
            Mono<Object> mono = Mono.fromSupplier(() -> client.createSchema(request))
                .handle((response, sink) -> LOG.info(
                    "Create new schema named '{}' into registry '{}'.",
                    change.getMetadata().getName(),
                    registryName
                ));
            results.add(toChangeResponse(change, mono.toFuture()));
        }

        return results;
    }
}
