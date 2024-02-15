/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.adapter;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating {@link ResourceReconcileRequest} instances.
 */
public final class ResourceReconcileRequestFactory {

    /**
     * Creates a new {@link ResourceReconcileRequest} for the specified resource and context.
     *
     * @param resource The resource.
     * @param context  The reconciliation context.
     * @param <T>      The type of the resource.
     * @return a new {@link ResourceReconcileRequest}.
     */
    public <T extends HasMetadata> @NotNull ResourceReconcileRequest create(@NotNull T resource,
                                                                            @NotNull ReconciliationContext context) {
        return create(List.of(resource), context);
    }

    /**
     * Creates a new {@link ResourceReconcileRequest} for the specified resources and context.
     *
     * @param resources The resources.
     * @param context   The reconciliation context.
     * @param <T>       The type of the resource.
     * @return a new {@link ResourceReconcileRequest}.
     */
    public <T extends HasMetadata> @NotNull ResourceReconcileRequest create(@NotNull List<T> resources,
                                                                            @NotNull ReconciliationContext context) {
        return new ResourceReconcileRequest(
                new ResourceReconcileRequest.Params(
                        context.annotations().asMap(),
                        context.labels().asMap(),
                        context.configuration().asMap(),
                        context.selector().getSelectorExpressions(),
                        context.selector().getSelectorMatchingStrategy()
                ), resources);
    }
}
