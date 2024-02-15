/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
     * @param mode       The reconciliation mode.
     * @param resources  The list of resources.
     * @param context    The reconciliation context.
     * @return the {@link ApiChangeResultList}.
     */
    ApiChangeResultList reconcile(ApiResourceIdentifier identifier,
                                  ReconciliationMode mode,
                                  List<HasMetadata> resources,
                                  ReconciliationContext context);

    /**
     * Reconciles the specified resources.
     *
     * @param mode      The reconciliation mode.
     * @param resources The list of resources.
     * @param context   The reconciliation context.
     * @return the {@link ApiChangeResultList}.
     */
    ApiChangeResultList patch(ReconciliationMode mode,
                              List<HasMetadata> resources,
                              ReconciliationContext context);

    /**
     * Gets the differences for the specified resources.
     *
     * @param context The reconciliation context.
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
