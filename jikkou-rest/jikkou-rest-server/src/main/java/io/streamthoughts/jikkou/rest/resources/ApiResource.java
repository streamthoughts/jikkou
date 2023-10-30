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

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.rest.adapters.ReconciliationContextAdapter;
import io.streamthoughts.jikkou.rest.controller.AbstractController;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import io.streamthoughts.jikkou.rest.entities.ResourceResponse;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import io.streamthoughts.jikkou.rest.services.ApiResourceService;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApiResource extends AbstractController {

    private final ReconciliationContextAdapter adapter;
    private final ApiResourceService service;

    @Inject
    public ApiResource(@NotNull ApiResourceService service,
                       @NotNull ReconciliationContextAdapter adapter) {
        this.service = Objects.requireNonNull(service, "service cannot be null");
        this.adapter =Objects.requireNonNull(adapter, "adapter cannot be null");
    }

    @Post(
            value = "/apis/{group}/{version}/{plural}/select",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YAML}
    )
    public HttpResponse<?> select(HttpRequest<?> httpRequest,
                                  @PathVariable("group") final String group,
                                  @PathVariable("version") final String version,
                                  @PathVariable("plural") final String name,
                                  HttpParameters parameters,
                                  @Body ResourceListRequest payload) {

        Map<String, Object> options = parameters.names()
                .stream()
                .collect(Collectors.toMap(Function.identity(), parameters::get));
        ResourceListRequest listRequest = payload.options(options);
        return doSelect(httpRequest, group, version, name, listRequest);
    }

    @Get(
            value = "/apis/{group}/{version}/{plural}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YAML}
    )
    public HttpResponse<?> list(HttpRequest<?> httpRequest,
                                @PathVariable("group") final String group,
                                @PathVariable("version") final String version,
                                @PathVariable("plural") final String plural,
                                HttpParameters parameters) {
        Map<String, Object> options = parameters.names()
                .stream()
                .collect(Collectors.toMap(Function.identity(), parameters::get));
        ResourceListRequest listRequest = new ResourceListRequest(options);
        return doSelect(httpRequest, group, version, plural, listRequest );
    }

    private MutableHttpResponse<?> doSelect(HttpRequest<?> httpRequest,
                                            String group,
                                            String version,
                                            String plural,
                                            ResourceListRequest requestBody) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        ResourceListObject<HasMetadata> result = service.search(
                identifier,
                adapter.getReconciliationContext(requestBody)
        );
        return HttpResponse.<ResourceListObject<?>>ok()
                .body(new ResourceResponse<>(result).link(Link.SELF, getSelfLink(httpRequest)));
    }

    @Post(value = "/apis/{group}/{version}/{plural}/validate",
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> validate(HttpRequest<?> httpRequest,
                                    @PathVariable("group") final String group,
                                    @PathVariable("version") final String version,
                                    @PathVariable("plural") final String plural,
                                    @Body ResourceReconcileRequest requestBody
    ) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        ResourceListObject<HasMetadata> result = service.validate(
                identifier,
                new ArrayList<>(requestBody.resources()),
                adapter.getReconciliationContext(requestBody, true)
        );

        return HttpResponse.<ResourceListObject<?>>ok()
                .body(new ResourceResponse<>(result).link(Link.SELF, getSelfLink(httpRequest)));
    }

    @Post(value = "/apis/{group}/{version}/{plural}/diff",
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> diff(HttpRequest<?> httpRequest,
                                    @PathVariable("group") final String group,
                                    @PathVariable("version") final String version,
                                    @PathVariable("plural") final String plural,
                                    @Body ResourceReconcileRequest requestBody
    ) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        ApiResourceChangeList result = service.diff(
                identifier,
                new ArrayList<>(requestBody.resources()),
                adapter.getReconciliationContext(requestBody, true)
        );

        return HttpResponse.<ResourceListObject<?>>ok()
                .body(new ResourceResponse<>(result).link(Link.SELF, getSelfLink(httpRequest)));
    }

    @Post(value = "/apis/{group}/{version}/{plural}/reconcile/mode/{mode}{?dry-run}",
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> reconcile(@PathVariable("group") final String group,
                                     @PathVariable("version") final String version,
                                     @PathVariable("plural") final String plural,
                                     @PathVariable("mode") final ReconciliationMode mode,
                                     @QueryValue(value = "dry-run", defaultValue = "false") final boolean dryRun,
                                     @Body ResourceReconcileRequest request
    ) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);

        ReconciliationContext context = adapter.getReconciliationContext(request, dryRun);
        ApiChangeResultList result = service.reconcile(
                identifier,
                mode,
                new ArrayList<>(request.resources()),
                context
        );
        return HttpResponse.<ResourceListObject<?>>ok().body(result);
    }
}
