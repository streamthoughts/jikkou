/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.resources;

import io.jikkou.core.models.ApiGroupList;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class ApiGroupListResourceTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldListGroupListResource() {
        ApiGroupList response = client.toBlocking().retrieve(HttpRequest.GET("/apis"), ApiGroupList.class);
        Assertions.assertNotNull(response);
    }

    @Test
    void shouldReturnNonEmptyGroupListWhenProvidersConfigured() {
        // When
        ApiGroupList response = client.toBlocking().retrieve(HttpRequest.GET("/apis"), ApiGroupList.class);

        // Then
        Assertions.assertEquals("ApiGroupList", response.kind());
        Assertions.assertNotNull(response.groups());
        Assertions.assertFalse(response.groups().isEmpty());
    }
}