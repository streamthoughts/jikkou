/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.resources;

import static io.streamthoughts.jikkou.common.utils.Enums.getForNamesIgnoreCase;
import static io.streamthoughts.jikkou.core.reconciler.SimpleResourceChangeFilter.FILTER_CHANGE_OP_NAME;
import static io.streamthoughts.jikkou.core.reconciler.SimpleResourceChangeFilter.FILTER_RESOURCE_OPS_NAME;
import static io.streamthoughts.jikkou.rest.adapters.HttpParametersAdapter.toMap;

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
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.SimpleResourceChangeFilter;
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
        this.adapter = Objects.requireNonNull(adapter, "adapter cannot be null");
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
        Map<String, Object> options = toMap(parameters);
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
        Map<String, Object> options = toMap(parameters);
        ResourceListRequest listRequest = new ResourceListRequest(options);
        return doSelect(httpRequest, group, version, plural, listRequest);
    }

    @Get(
        value = "/apis/{group}/{version}/{plural}/{name}",
        produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YAML}
    )
    public HttpResponse<?> get(HttpRequest<?> httpRequest,
                               @PathVariable("group") final String group,
                               @PathVariable("version") final String version,
                               @PathVariable("plural") final String plural,
                               @PathVariable("name") final String name,
                               HttpParameters parameters) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        Map<String, Object> options = toMap(parameters);
        HasMetadata result = service.get(identifier, name, Configuration.from(options));
        return HttpResponse.<HasMetadata>ok()
            .body(new ResourceResponse<>(result).link(Link.SELF, getSelfLink(httpRequest)));
    }


    private MutableHttpResponse<?> doSelect(HttpRequest<?> httpRequest,
                                            String group,
                                            String version,
                                            String plural,
                                            ResourceListRequest requestBody) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        ResourceList<HasMetadata> result = service.search(
            identifier,
            adapter.getReconciliationContext(requestBody)
        );
        return HttpResponse.<ResourceList<?>>ok()
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
        ResourceList<HasMetadata> result = service.validate(
            identifier,
            new ArrayList<>(requestBody.resources()),
            adapter.getReconciliationContext(requestBody, true)
        );

        return HttpResponse.<ResourceList<?>>ok()
            .body(new ResourceResponse<>(result).link(Link.SELF, getSelfLink(httpRequest)));
    }

    @Post(value = "/apis/{group}/{version}/{plural}/diff{?" + FILTER_RESOURCE_OPS_NAME + "," + FILTER_CHANGE_OP_NAME + "}",
        produces = MediaType.APPLICATION_JSON,
        consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> diff(HttpRequest<?> httpRequest,
                                @PathVariable("group") final String group,
                                @PathVariable("version") final String version,
                                @PathVariable("plural") final String plural,
                                @QueryValue(value = FILTER_RESOURCE_OPS_NAME, defaultValue = "") final String keepChangesOp,
                                @QueryValue(value = FILTER_CHANGE_OP_NAME, defaultValue = "") final String keepStatesOp,
                                @Body ResourceReconcileRequest requestBody
    ) {
        ApiResourceIdentifier identifier = new ApiResourceIdentifier(group, version, plural);
        ApiResourceChangeList result = service.diff(
            identifier,
            new ArrayList<>(requestBody.resources()),
            new SimpleResourceChangeFilter()
                .filterOutAllResourcesExcept(getForNamesIgnoreCase(keepChangesOp, Operation.class))
                .filterOutAllChangesExcept(getForNamesIgnoreCase(keepStatesOp, Operation.class)),
            adapter.getReconciliationContext(requestBody, true)
        );

        return HttpResponse.<ApiResourceChangeList>ok()
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
        return HttpResponse.<ResourceList<?>>ok().body(result);
    }

    @Post(value = "/api/v1/resources/patch/mode/{mode}{?dry-run}",
        produces = MediaType.APPLICATION_JSON,
        consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> patch(@PathVariable("mode") final ReconciliationMode mode,
                                 @QueryValue(value = "dry-run", defaultValue = "false") final boolean dryRun,
                                 @Body ResourceReconcileRequest request
    ) {
        ReconciliationContext context = adapter.getReconciliationContext(request, dryRun);
        ApiChangeResultList result = service.patch(
            mode,
            new ArrayList<>(request.resources()),
            context
        );
        return HttpResponse.<ResourceList<?>>ok().body(result);
    }
}
