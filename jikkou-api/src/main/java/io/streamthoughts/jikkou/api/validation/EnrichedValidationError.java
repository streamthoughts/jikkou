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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.CoreAnnotations;
import io.streamthoughts.jikkou.api.model.HasMetadata;
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
