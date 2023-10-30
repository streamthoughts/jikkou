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
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.rest.controller.AbstractController;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Controller("/apis/core.jikkou.io/v1/healths")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiHealthIndicatorListResource extends AbstractController {

    public static final String STATUS_LINK_KEY = "status";
    private final JikkouApi api;
    private final Map<String, ApiHealthIndicator> indicatorsByNames;

    @Inject
    public ApiHealthIndicatorListResource(@NotNull JikkouApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
        this.indicatorsByNames = api.getApiHealthIndicators().indicators()
                .stream()
                .collect(Collectors.toMap(ApiHealthIndicator::name, Function.identity()));

    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public ResourceResponse<ApiHealthIndicatorList> get(HttpRequest<?> request) {
        ApiHealthIndicatorList apiHealthIndicatorList = api.getApiHealthIndicators();
        List<ApiHealthIndicator> enriched = apiHealthIndicatorList
                .indicators()
                .stream()
                .map(indicator -> {
                    return new ApiHealthIndicator(
                            indicator.name(),
                            indicator.description(),
                            getMetadata(request, indicator)
                    );
                })
                .toList();
        return new ResourceResponse<>(new ApiHealthIndicatorList(enriched))
                .link(Link.SELF, getSelfLink(request));
    }


    @Get(value = "/{name}/status{?timeout}", produces = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public HttpResponse<?> get(HttpRequest<?> request,
                               @PathParam("name") String name,
                               @QueryValue(value = "timeout", defaultValue = "5000") long timeout) {
        if (!indicatorsByNames.containsKey(name)) {
            return HttpResponse.notFound(new ErrorResponse(
                    "HealthIndicator Not Found",
                    List.of(new ErrorEntity(
                                    HttpStatus.NOT_FOUND.getCode(),
                                    Errors.API_HEALTH_INDICATOR_NOT_FOUND,
                                    String.format(
                                            "Health indicator for name '%s' is unknown.",
                                            name
                                    )
                            )
                    )
            ));
        }

        ApiHealthResult apiHealth = api.getApiHealth(name, Duration.ofMillis(timeout));
        return HttpResponse.ok(new ResourceResponse<>(apiHealth)
                .link(Link.SELF, getSelfLink(request)));
    }

    @NotNull
    private Map<String, Object> getMetadata(HttpRequest<?> httpRequest,
                                            ApiHealthIndicator indicator) {
        Map<String, Object> metadata = new HashMap<>(indicator.metadata());
        metadata.put(Resource.LINKS, Map.of(STATUS_LINK_KEY, getLinkForStatus(httpRequest, indicator)));
        return metadata;
    }

    private Link getLinkForStatus(HttpRequest<?> httpRequest, ApiHealthIndicator indicator) {
        Link link = getLink(httpRequest, indicator.name(), STATUS_LINK_KEY);
        return Link.build(link.getHref() + "{?timeout}")
                .templated(true)
                .build();
    }
}
