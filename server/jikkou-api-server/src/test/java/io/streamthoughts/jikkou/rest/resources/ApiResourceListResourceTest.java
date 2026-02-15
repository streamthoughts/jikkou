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
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class ApiResourceListResourceTest {

    public static final String JIKKOU_API = "/apis/kafka.jikkou.io/v1beta2";

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldListApiResources() {
        ApiResourceList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiResourceList.class
        );
        Assertions.assertNotNull(response);
    }

    @Test
    void shouldReturnResourcesWithExpectedKindWhenQueried() {
        // When
        ApiResourceList response = client.toBlocking().retrieve(
                HttpRequest.GET(JIKKOU_API),
                ApiResourceList.class
        );

        // Then
        Assertions.assertEquals("ApiResourceList", response.kind());
        Assertions.assertNotNull(response.resources());
        Assertions.assertFalse(response.resources().isEmpty());
    }
}