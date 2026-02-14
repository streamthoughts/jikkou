/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.adapter;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceReconcileRequestFactoryTest {

    @Test
    void shouldCreateRequestWithProviderName() {
        // Given
        ResourceReconcileRequestFactory factory = new ResourceReconcileRequestFactory();
        ReconciliationContext context = ReconciliationContext.builder()
                .dryRun(false)
                .selector(SelectorMatchingStrategy.ALL.combines(Collections.emptyList()))
                .providerName("kafka-prod")
                .build();

        // When
        ResourceReconcileRequest request = factory.create(Collections.emptyList(), context);

        // Then
        Assertions.assertNotNull(request);
        Assertions.assertEquals("kafka-prod", request.provider());
    }

    @Test
    void shouldCreateRequestWithNullProviderName() {
        // Given
        ResourceReconcileRequestFactory factory = new ResourceReconcileRequestFactory();
        ReconciliationContext context = ReconciliationContext.builder()
                .dryRun(false)
                .selector(SelectorMatchingStrategy.ALL.combines(Collections.emptyList()))
                .build();

        // When
        ResourceReconcileRequest request = factory.create(Collections.emptyList(), context);

        // Then
        Assertions.assertNotNull(request);
        Assertions.assertNull(request.provider());
    }
}
