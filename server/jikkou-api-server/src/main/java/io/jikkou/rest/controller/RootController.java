/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.controller;

import io.jikkou.rest.Project;
import io.jikkou.rest.data.Info;
import io.jikkou.rest.entities.ResourceResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/")
@Secured(SecurityRule.IS_ANONYMOUS)
public class RootController extends AbstractController {

    @Get(produces = MediaType.APPLICATION_JSON)
    public ResourceResponse<Info> get(HttpRequest<?> httpRequest) {
        return new ResourceResponse<>(Project.info())
                .link(Link.SELF, getSelfLink(httpRequest))
                .link("get-apis", getLink(httpRequest, "apis"));
    }
}
