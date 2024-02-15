/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change.handler;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteSchemaSubjectChangeHandler extends AbstractSchemaSubjectChangeHandler implements ChangeHandler<ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteSchemaSubjectChangeHandler.class);

    /**
     * Creates a new {@link DeleteSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public DeleteSchemaSubjectChangeHandler(@NotNull final AsyncSchemaRegistryApi api) {
        super(api);
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
            final String subject = change.getMetadata().getName();
            SchemaSubjectChangeOptions options = getSchemaSubjectChangeOptions(change);
            CompletableFuture<Void> future = api.deleteSubjectVersions(subject, options.permanentDelete())
                    .thenApplyAsync(versions -> {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                    "Deleted all versions for Schema Registry subject '{}': {}",
                                    subject,
                                    versions
                            );
                        }
                        return null;
                    });
            results.add(toChangeResponse(change, future));
        }
        return results;
    }
}
