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
package io.streamthoughts.jikkou.rest.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.uri.UriBuilder;

public class AbstractController {

    public Link getSelfLink(HttpRequest<?> request) {
        return getLink(request);
    }

    public Link getLink(HttpRequest<?> request, String... paths) {
        return getLinkBuilder(request, paths).build();
    }

    public Link.Builder getLinkBuilder(HttpRequest<?> request, String... paths) {

        UriBuilder builder = UriBuilder.of(request.getUri());
        for (String path : paths) {
            builder = builder.path(path);
        }
        return Link.build(builder.build());
    }
}
