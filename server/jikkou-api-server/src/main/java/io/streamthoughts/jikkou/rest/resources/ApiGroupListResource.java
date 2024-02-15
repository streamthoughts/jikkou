/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.resources;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.rest.controller.AbstractController;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import jakarta.annotation.security.PermitAll;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Controller("/apis")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiGroupListResource extends AbstractController {

    private final JikkouApi api;

    public ApiGroupListResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiGroupList> get(HttpRequest<?> request) {
        ApiGroupList apiGroup = api.listApiGroups();
        List<ApiGroup> enriched = apiGroup.groups()
                .stream()
                .map(group -> {
                    Set<ApiGroupVersion> versions = group.versions()
                            .stream()
                            .map(version -> new ApiGroupVersion(
                                            version.groupVersion(),
                                            version.version(),
                                            getMetadata(request, version)
                                    )
                            )
                            .collect(Collectors.toSet());
                    return new ApiGroup(group.name(), versions);
                })
                .toList();
        return new ResourceResponse<>(new ApiGroupList(enriched))
                .link(Link.SELF, getSelfLink(request));
    }

    @NotNull
    private HashMap<String, Object> getMetadata(HttpRequest<?> httpRequest, ApiGroupVersion version) {
        HashMap<String, Object> metadata = new HashMap<>(version.metadata());
        metadata.put(Resource.LINKS, Map.of(Link.SELF, getLink(httpRequest, version.groupVersion())));
        return metadata;
    }
}
