/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import org.jetbrains.annotations.NotNull;


public class ResourceSelector implements Selector {

    private final String apiVersion;
    private final String kind;

    public ResourceSelector(final String apiVersion, final String kind) {
        this.apiVersion = apiVersion;
        this.kind = kind;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        boolean matched = true;

        if (apiVersion != null) {
            matched = apiVersion.equals(resource.getApiVersion());
        }

        if (kind != null) {
            matched = matched && kind.equals(resource.getKind());
        }

        return matched;
    }
}
