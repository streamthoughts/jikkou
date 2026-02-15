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
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class ApiActionResourceTest {

    public static final String JIKKOU_API = "/api/v1/actions";

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldListActions() {
        ApiExtensionList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiExtensionList.class
        );
        Assertions.assertNotNull(response);
    }

    @Test
    void shouldReturnExpectedKindWhenActionsListed() {
        // When
        ApiExtensionList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiExtensionList.class
        );

        // Then
        Assertions.assertEquals("ApiExtensionList", response.kind());
        Assertions.assertNotNull(response.extensions());
    }
}