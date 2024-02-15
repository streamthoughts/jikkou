/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.adapters;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.selector.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReconciliationContextAdapterTest {

    @Test
    void shouldGetCtxForResourceReconcileRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new ExpressionSelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceReconcileRequest(
                new ResourceReconcileRequest.Params(),
                Collections.emptyList()
        ), false);
        Assertions.assertNotNull(context);
    }

    @Test
    void shouldGetCtxForResourceListRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new ExpressionSelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceListRequest());
        Assertions.assertNotNull(context);

    }
}