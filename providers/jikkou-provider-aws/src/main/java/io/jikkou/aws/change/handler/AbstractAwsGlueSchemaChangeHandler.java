/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.aws.change.handler;

import io.jikkou.aws.change.AwsGlueSchemaChangeDescription;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.TextDescription;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.GlueClient;

public abstract class AbstractAwsGlueSchemaChangeHandler implements ChangeHandler {

    protected final GlueClient client;

    /**
     * Creates a new {@link AbstractAwsGlueSchemaChangeHandler} instance.
     *
     * @param client the {@link GlueClient} instance.
     */
    public AbstractAwsGlueSchemaChangeHandler(@NotNull final GlueClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    public ChangeResponse toChangeResponse(ResourceChange change, CompletableFuture<?> future) {
        CompletableFuture<ChangeMetadata> handled = future.handle((unused, throwable) -> {
            if (throwable == null) {
                return ChangeMetadata.empty();
            }

            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
            return ChangeMetadata.of(throwable);
        });
        return new ChangeResponse(change, handled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange item) {
        return new AwsGlueSchemaChangeDescription(item);
    }
}
