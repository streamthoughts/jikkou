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
package io.streamthoughts.jikkou.http.client.adapter;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ResourceReconcileRequestFactory {

    public <T extends HasMetadata> @NotNull ResourceReconcileRequest create(@NotNull T resource,
                                                                            @NotNull ReconciliationContext context) {
        return create(List.of(resource), context);
    }

    public <T extends HasMetadata> @NotNull ResourceReconcileRequest create(@NotNull List<T> resources,
                                                                            @NotNull ReconciliationContext context) {
        return new ResourceReconcileRequest(
                new ResourceReconcileRequest.Params(
                        context.annotations().asMap(),
                        context.labels().asMap(),
                        context.configuration().asMap(),
                        new AggregateSelector(context.selectors()).getSelectorExpressions()
                ), resources);
    }
}
