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
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionSpec;
import io.streamthoughts.jikkou.core.models.Verb;
import io.streamthoughts.jikkou.rest.controller.AbstractController;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import jakarta.annotation.security.PermitAll;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

@Controller("/apis/{groupName}/{version}")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiResourceListResource extends AbstractController {

    private final JikkouApi api;

    public ApiResourceListResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiResourceList> get(HttpRequest<?> httpRequest,
                                                 @PathVariable("groupName") final String group,
                                                 @PathVariable("version") final String version) {

        ApiResourceList apiResourceList = api.listApiResources(group, version);
        List<ApiResource> resources = apiResourceList.resources().stream()
                .map(resource -> {
                    Map<String, Object> metadata = getMetadata(httpRequest, resource);
                    return new ApiResource(
                            resource.name(),
                            resource.kind(),
                            resource.singularName(),
                            resource.shortNames(),
                            resource.description(),
                            resource.verbs(),
                            resource.verbsOptions(),
                            metadata
                    );
                })
                .toList();
        ApiResourceList resource = new ApiResourceList(apiResourceList.groupVersion(), resources);
        return new ResourceResponse<>(resource).link(Link.SELF, getSelfLink(httpRequest));
    }

    @NotNull
    private Map<String, Object> getMetadata(HttpRequest<?> httpRequest,
                                                ApiResource resource) {

        Map<String, Link> links = new LinkedHashMap<>();

        // Links for Verb LIST
        if (resource.isVerbSupported(Verb.LIST)) {
            links.put("list", getLinkForList(httpRequest, resource, false));
            links.put("select", getLinkForSelect(httpRequest, resource));

            if (resource.getVerbOptionList(Verb.LIST).isPresent()) {
                links.put("list-params", getLinkForList(httpRequest, resource, true));
            }
        }

        // Links for Verb GET
        if (resource.isVerbSupported(Verb.GET)) {
            links.put("get", getLinkForGet(httpRequest, resource, false));
            if (resource.getVerbOptionList(Verb.GET).isPresent()) {
                links.put("get-params", getLinkForGet(httpRequest, resource, true));
            }
        }

        if (resource.isVerbSupported(Verb.CREATE) ||
                resource.isVerbSupported(Verb.UPDATE) ||
                resource.isVerbSupported(Verb.DELETE) ||
                resource.isVerbSupported(Verb.APPLY)
        ) {
            links.put("reconcile", getLinkForReconcile(httpRequest, resource));
            links.put("validate", getLinkForValidate(httpRequest, resource));
            links.put("diff", getLinkForDiff(httpRequest, resource));
        }

        Map<String, Object> metadata = new HashMap<>(resource.metadata());
        metadata.put(ResourceResponse.LINKS, links);
        return metadata;
    }

    private Link getLinkForGet(HttpRequest<?> httpRequest, ApiResource resource, boolean includeOptions) {
        Link link = getLink(httpRequest, resource.name());
        Link build = Link.build(link.getHref() + "/{name}")
                .templated(true)
                .build();
        return addLinkOptions(build, resource, Verb.LIST, includeOptions);
    }

    private Link getLinkForSelect(HttpRequest<?> httpRequest, ApiResource resource) {
        final Link link = getLink(httpRequest, resource.name(), "select");
        return addLinkOptions(link, resource, Verb.LIST, false);
    }

    private Link getLinkForList(HttpRequest<?> httpRequest, ApiResource resource, boolean includeOptions) {
        final Link link = getLink(httpRequest, resource.name());
        return addLinkOptions(link, resource, Verb.LIST, includeOptions);
    }

    private static Link addLinkOptions(Link link, ApiResource resource, Verb verb, boolean includeOptionals) {
        List<String> options = resource.getVerbOptionList(verb)
                .map(optionList -> optionList.options()
                        .stream()
                        .filter(spec -> includeOptionals || spec.required())
                        .map(ApiResourceVerbOptionSpec::name)
                ).orElse(Stream.empty()).toList();
        if (options.isEmpty()) {
            return link;
        }
        String template = options.stream()
                .collect(Collectors.joining(",", "{?", "}"));
        return Link.build(link.getHref() + template)
                .templated(true)
                .build();
    }

    private Link getLinkForDiff(HttpRequest<?> httpRequest, ApiResource resource) {
        return getLinkBuilder(httpRequest, resource.name(), "diff")
                .templated(false)
                .build();
    }

    private Link getLinkForValidate(HttpRequest<?> httpRequest, ApiResource resource) {
        return getLinkBuilder(httpRequest, resource.name(), "validate")
                .templated(false)
                .build();
    }

    private Link getLinkForReconcile(HttpRequest<?> httpRequest, ApiResource resource) {
        Link link = getLink(httpRequest, resource.name(), "reconcile", "mode");
        return Link.build(link.getHref() + "/{mode}{?dry-run}")
                .templated(true)
                .build();
    }
}
