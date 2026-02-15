/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.resources;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class ApiHealthIndicatorListResourceTest {

    public static final String JIKKOU_API = "/api/v1/healths";

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldListHealthIndicators() {
        ApiHealthIndicatorList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiHealthIndicatorList.class
        );
        Assertions.assertNotNull(response);
    }

    @Test
    void shouldReturnExpectedKindWhenHealthIndicatorsListed() {
        // When
        ApiHealthIndicatorList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiHealthIndicatorList.class
        );

        // Then
        Assertions.assertEquals("ApiHealthIndicatorList", response.kind());
        Assertions.assertNotNull(response.indicators());
    }
}