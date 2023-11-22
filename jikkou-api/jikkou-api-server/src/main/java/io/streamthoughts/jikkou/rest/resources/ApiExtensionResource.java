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
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.rest.controller.AbstractController;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Controller("/api/v1/extensions")
@Secured(SecurityRule.IS_AUTHENTICATED)
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

        if (!Strings.isBlank(category)) {
            extensions = api.getApiExtensions(ExtensionCategory.getForNameIgnoreCase(category));
        }else if (!Strings.isBlank(type)) {
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
