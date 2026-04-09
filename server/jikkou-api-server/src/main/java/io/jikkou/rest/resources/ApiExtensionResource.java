/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.resources;

import io.jikkou.common.utils.Strings;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.extension.ExtensionCategory;
import io.jikkou.core.models.ApiExtension;
import io.jikkou.core.models.ApiExtensionList;
import io.jikkou.rest.controller.AbstractController;
import io.jikkou.rest.entities.ResourceResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.Link;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Controller("/api/v1/extensions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ApiExtensionResource extends AbstractController {

    private static final String NO_VALUE = "";
    private final JikkouApi api;

    @Inject
    public ApiExtensionResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiExtensionList> list(HttpRequest<?> request,
                                                   @QueryValue(value = "type", defaultValue = NO_VALUE) String type,
                                                   @QueryValue(value = "category", defaultValue = NO_VALUE) String category) {

        ApiExtensionList extensions;

        if (!Strings.isNullOrEmpty(category)) {
            extensions = api.getApiExtensions(ExtensionCategory.getForNameIgnoreCase(category));
        }else if (!Strings.isNullOrEmpty(type)) {
            extensions = api.getApiExtensions(type);
        } else {
            extensions = api.getApiExtensions();
        }
        return new ResourceResponse<>(extensions).link(Link.SELF, getSelfLink(request));
    }

    @Get(value = "/{name}", produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiExtension> get(HttpRequest<?> request,
                                              @PathVariable("name") String name) {
        ApiExtension result = api.getApiExtension(name);
        return new ResourceResponse<>(result).link(Link.SELF, getSelfLink(request));
    }
}
