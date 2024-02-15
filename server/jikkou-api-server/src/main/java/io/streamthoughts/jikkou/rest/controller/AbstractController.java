/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
