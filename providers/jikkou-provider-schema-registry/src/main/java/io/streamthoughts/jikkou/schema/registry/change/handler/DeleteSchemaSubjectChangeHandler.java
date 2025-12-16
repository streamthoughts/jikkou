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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class DeleteSchemaSubjectChangeHandler extends AbstractSchemaSubjectChangeHandler implements ChangeHandler {

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
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
        List<ChangeResponse> results = new ArrayList<>();
        for (ResourceChange change : changes) {
            final String subject = change.getMetadata().getName();
            SchemaSubjectChangeOptions options = getSchemaSubjectChangeOptions(change);
            Mono<Void> mono = api
                    .deleteSubjectVersions(subject, options.permanentDelete())
                    .handle((versions, sink) -> {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                    "Deleted all versions for Schema Registry subject '{}': {}",
                                    subject,
                                    versions
                            );
                        }
                    });
            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }
}
