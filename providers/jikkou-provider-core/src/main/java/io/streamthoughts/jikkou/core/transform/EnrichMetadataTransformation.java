/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResources;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Priority(HasPriority.HIGHEST_PRECEDENCE)
@Enabled
@SupportedResources
public class EnrichMetadataTransformation implements Transformation<HasMetadata> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform,
                                                    @NotNull HasItems resources,
                                                    @NotNull ReconciliationContext context) {
        ObjectMeta metadata = toTransform.optionalMetadata().orElse(new ObjectMeta());
        if (!context.labels().isEmpty()) {
            Map<String, Object> labels = context.labels().asMap();
            metadata = metadata.toBuilder()
                    .withLabels(labels)
                    .build();
        }

        if (!context.annotations().isEmpty()) {
            Map<String, Object> annotations = context.annotations().asMap();
            metadata = metadata.toBuilder()
                    .withAnnotations(annotations)
                    .build();
        }
        return Optional.of(toTransform.withMetadata(metadata));
    }
}
