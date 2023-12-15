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
package io.streamthoughts.jikkou.rest.services;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import java.util.List;

public interface ApiResourceService {

    /**
     * Reconciles the specified resources.
     *
     * @param identifier The resource identifier.
     * @param context    The reconciliation context.
     * @return an optional
     */
    ApiChangeResultList reconcile(ApiResourceIdentifier identifier,
                                  ReconciliationMode mode,
                                  List<HasMetadata> resources,
                                  ReconciliationContext context);

    /**
     * Gets the differences for the specified resources.
     *
     * @param identifier The resource identifier.
     * @param context    The reconciliation context.
     * @return an optional
     */
    ApiResourceChangeList diff(ApiResourceIdentifier identifier,
                               List<HasMetadata> resources,
                               ResourceChangeFilter filter,
                               ReconciliationContext context);

    /**
     * Validates the specified resources.
     *
     * @param identifier The resource identifier.
     * @param context    The reconciliation context.
     * @return an optional
     */
    ResourceListObject<HasMetadata> validate(ApiResourceIdentifier identifier,
                                             List<HasMetadata> resources,
                                             ReconciliationContext context);

    /**
     * Search resources for the specified identifier and context.
     *
     * @param identifier The resource identifier.
     * @param context    The reconciliation context.
     * @return an optional
     */
    ResourceListObject<HasMetadata> search(ApiResourceIdentifier identifier,
                                           ReconciliationContext context);

    /**
     * Gets the resource for the specified identifier and name.
     *
     * @param identifier    The resource identifier.
     * @param name          The resource name.
     * @param configuration The configuration.
     * @return an optional
     */
    HasMetadata get(ApiResourceIdentifier identifier,
                    String name,
                    Configuration configuration);
}
