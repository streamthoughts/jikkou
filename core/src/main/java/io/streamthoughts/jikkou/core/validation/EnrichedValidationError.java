/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnrichedValidationError extends ValidationError {

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param name     the validation constraint name.
     * @param resource the original resource.
     * @param message  the error message.
     * @param details  the error details.
     */
    public EnrichedValidationError(@Nullable String name,
                                   @Nullable HasMetadata resource,
                                   @NotNull String message,
                                   @NotNull Map<String, Object> details) {
        super(name, resource, message, details);
    }

    /** {@inheritDoc} **/
    @Override
    public @NotNull Map<String, Object> details() {
        return enrichedDetails(resource());
    }

    @NotNull
    private <T extends HasMetadata> Map<String, Object> enrichedDetails(@Nullable T resource) {
        Map<String, Object> details = new HashMap<>();
        if (resource != null) {
            Stream.of(
                            CoreAnnotations.JKKOU_IO_MANAGED_BY_LOCATION
                    )
                    .forEach(annot -> HasMetadata.getMetadataAnnotation(resource, annot).
                            ifPresent(val -> details.put(annot, val))
                    );
        }
        return details;
    }
}
