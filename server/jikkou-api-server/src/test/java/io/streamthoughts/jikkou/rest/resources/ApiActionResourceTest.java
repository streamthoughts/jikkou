/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
}