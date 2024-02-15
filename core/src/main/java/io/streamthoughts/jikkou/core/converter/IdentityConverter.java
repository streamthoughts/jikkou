/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Identity converter.
 */
public class IdentityConverter implements Converter<HasMetadata, HasMetadata> {

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<HasMetadata> apply(@NotNull HasMetadata resource) {
        return List.of(resource);
    }
}