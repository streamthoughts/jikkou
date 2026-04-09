/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.rest.resources;

import io.jikkou.core.JikkouApi;
import io.jikkou.core.models.ApiResourceSchema;
import io.jikkou.core.models.ResourceType;
import io.jikkou.rest.entities.ResourceResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Controller("/apis/{groupName}/{version}/schema")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiResourceSchemaResource {

    private final JikkouApi api;

    public ApiResourceSchemaResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    @Get(value = "/{kind}", produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiResourceSchema> get(@PathVariable("groupName") final String group,
                                                   @PathVariable("version") final String version,
                                                   @PathVariable("kind") final String kind) {
        ResourceType resourceType = ResourceType.of(kind, group + "/" + version);
        ApiResourceSchema schema = api.getResourceSchema(resourceType);
        return new ResourceResponse<>(schema);
    }
}
