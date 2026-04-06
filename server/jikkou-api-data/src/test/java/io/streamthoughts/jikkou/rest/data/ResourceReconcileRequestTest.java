/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceReconcileRequestTest {

    @Test
    void shouldGetEmptyForNoArgs() {
        ResourceReconcileRequest request = new ResourceReconcileRequest();
        Assertions.assertNotNull(request.resources());
        Assertions.assertNotNull(request.params());
        Assertions.assertNotNull(request.providers());
        Assertions.assertFalse(request.continueOnError());
    }

    @Test
    void shouldGetProvidersList() {
        ResourceReconcileRequest request = new ResourceReconcileRequest(
            null, null, null, List.of("kafka-prod", "kafka-staging"), null);
        Assertions.assertEquals(List.of("kafka-prod", "kafka-staging"), request.providers());
    }

    @Test
    void shouldDefaultContinueOnErrorToFalse() {
        ResourceReconcileRequest request = new ResourceReconcileRequest(null, null, null, null, null);
        Assertions.assertFalse(request.continueOnError());
    }

    @Test
    void shouldSetContinueOnError() {
        ResourceReconcileRequest request = new ResourceReconcileRequest(
            null, null, null, List.of("kafka-prod"), true);
        Assertions.assertTrue(request.continueOnError());
    }

    @Test
    void shouldBackwardsCompatibleConstructorWork() {
        ResourceReconcileRequest request = new ResourceReconcileRequest(null, null, "kafka-prod");
        Assertions.assertEquals("kafka-prod", request.provider());
        Assertions.assertTrue(request.providers().isEmpty());
        Assertions.assertFalse(request.continueOnError());
    }
}
