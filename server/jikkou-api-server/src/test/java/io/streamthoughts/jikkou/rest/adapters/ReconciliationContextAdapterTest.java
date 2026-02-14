/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.adapters;

import io.streamthoughts.jikkou.core.ListContext;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.selector.SelectorFactory;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReconciliationContextAdapterTest {

    @Test
    void shouldGetCtxForResourceReconcileRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceReconcileRequest(
            new ResourceReconcileRequest.Params(),
            Collections.emptyList()
        ), false);
        Assertions.assertNotNull(context);
    }

    @Test
    void shouldGetCtxForResourceListRequest() {
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ReconciliationContext context = adapter.getReconciliationContext(new ResourceListRequest());
        Assertions.assertNotNull(context);
    }

    @Test
    void shouldGetListContextForResourceListRequest() {
        // Given
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ResourceListRequest request = new ResourceListRequest();

        // When
        ListContext context = adapter.getListContext(request);

        // Then
        Assertions.assertNotNull(context);
        Assertions.assertNotNull(context.selector());
        Assertions.assertNotNull(context.configuration());
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldGetListContextWithProvider() {
        // Given
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ResourceListRequest request = new ResourceListRequest(
            Map.of("key", "value"),
            Collections.emptyList(),
            SelectorMatchingStrategy.ALL,
            "kafka-prod"
        );

        // When
        ListContext context = adapter.getListContext(request);

        // Then
        Assertions.assertNotNull(context);
        Assertions.assertEquals("kafka-prod", context.providerName());
        Assertions.assertEquals("value", context.configuration().getString("key"));
    }

    @Test
    void shouldGetReconciliationContextWithProviderFromReconcileRequest() {
        // Given
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ResourceReconcileRequest request = new ResourceReconcileRequest(
            new ResourceReconcileRequest.Params(),
            Collections.emptyList(),
            "kafka-prod"
        );

        // When
        ReconciliationContext context = adapter.getReconciliationContext(request, false);

        // Then
        Assertions.assertNotNull(context);
        Assertions.assertEquals("kafka-prod", context.providerName());
    }

    @Test
    void shouldGetReconciliationContextWithNullProviderFromReconcileRequest() {
        // Given
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ResourceReconcileRequest request = new ResourceReconcileRequest(
            new ResourceReconcileRequest.Params(),
            Collections.emptyList()
        );

        // When
        ReconciliationContext context = adapter.getReconciliationContext(request, false);

        // Then
        Assertions.assertNotNull(context);
        Assertions.assertNull(context.providerName());
    }

    @Test
    void shouldGetReconciliationContextWithProviderFromResourceListRequest() {
        // Given
        ReconciliationContextAdapter adapter = new ReconciliationContextAdapter(new SelectorFactory());
        ResourceListRequest request = new ResourceListRequest(
            Map.of("key", "value"),
            Collections.emptyList(),
            SelectorMatchingStrategy.ALL,
            "kafka-prod"
        );

        // When
        ReconciliationContext context = adapter.getReconciliationContext(request);

        // Then
        Assertions.assertNotNull(context);
        Assertions.assertEquals("kafka-prod", context.providerName());
    }
}