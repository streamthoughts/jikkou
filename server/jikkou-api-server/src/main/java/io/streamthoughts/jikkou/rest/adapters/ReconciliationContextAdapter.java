/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.adapters;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.selector.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 *
 */
@Singleton
public final class ReconciliationContextAdapter {

    private final ExpressionSelectorFactory selectorFactory;

    @Inject
    public ReconciliationContextAdapter(ExpressionSelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }

    public ReconciliationContext getReconciliationContext(ResourceReconcileRequest request, boolean dryRun) {
        ResourceReconcileRequest.Params params = request.params();
        Selector selector = params.selectorMatchingStrategy()
                .combines(selectorFactory.make(params.selectors()));

        return ReconciliationContext
                .builder()
                .dryRun(dryRun)
                .configuration(Configuration.from(params.options()))
                .selector(selector)
                .annotations(NamedValueSet.setOf(params.annotations()))
                .labels(NamedValueSet.setOf(params.labels()))
                .build();
    }

    public ReconciliationContext getReconciliationContext(ResourceListRequest request) {
        Selector selector = request.selectorMatchingStrategy()
                .combines(selectorFactory.make(request.selectors()));
        return ReconciliationContext
                .builder()
                .dryRun(true)
                .configuration(Configuration.from(request.options()))
                .selector(selector)
                .build();
    }
}
