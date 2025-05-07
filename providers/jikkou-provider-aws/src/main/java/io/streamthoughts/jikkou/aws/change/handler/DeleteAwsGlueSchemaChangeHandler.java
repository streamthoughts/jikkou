/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.change.handler;

import io.streamthoughts.jikkou.aws.AwsGlueAnnotations;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
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
import software.amazon.awssdk.services.glue.model.DeleteSchemaRequest;
import software.amazon.awssdk.services.glue.model.SchemaId;

public class DeleteAwsGlueSchemaChangeHandler extends AbstractAwsGlueSchemaChangeHandler implements ChangeHandler<ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteAwsGlueSchemaChangeHandler.class);

    /**
     * Creates a new {@link DeleteAwsGlueSchemaChangeHandler} instance.
     *
     * @param client the {@link GlueClient} instance.
     */
    public DeleteAwsGlueSchemaChangeHandler(@NotNull final GlueClient client) {
        super(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return Set.of(Operation.DELETE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
        List<ChangeResponse<ResourceChange>> results = new ArrayList<>();
        for (ResourceChange change : changes) {

            final String schemaName = change.getMetadata().getName();

            final String registryName = (String) change.getMetadata()
                .getLabelByKey(AwsGlueAnnotations.SCHEMA_REGISTRY_NAME)
                .getValue();

            DeleteSchemaRequest deleteRequest = DeleteSchemaRequest.builder()
                .schemaId(SchemaId.builder()
                    .registryName(registryName)
                    .schemaName(schemaName)
                    .build())
                .build();

            Mono<Object> mono = Mono.fromSupplier(() -> client.deleteSchema(deleteRequest))
                .handle((versions, sink) -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                            "Deleted schema '{}' from registry name {}",
                            schemaName,
                            versions
                        );
                    }
                });
            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }
}
