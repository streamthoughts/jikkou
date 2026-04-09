/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.resources;

import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiProvider;
import io.jikkou.core.models.ApiProviderList;
import io.jikkou.rest.controller.AbstractController;
import io.jikkou.rest.entities.ResourceResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.Link;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Controller("/api/v1/providers")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ApiProviderResource extends AbstractController {

    private final JikkouApi api;

    @Inject
    public ApiProviderResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiProviderList> list(HttpRequest<?> request) {
        ApiProviderList providers = api.getApiProviders();
        return new ResourceResponse<>(providers).link(Link.SELF, getSelfLink(request));
    }

    @Get(uri = "/{name}", produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiProvider> get(HttpRequest<?> request,
                                             @PathVariable("name") String name) {
        ApiProvider provider = api.getApiProvider(name);
        return new ResourceResponse<>(provider).link(Link.SELF, getSelfLink(request));
    }
}
