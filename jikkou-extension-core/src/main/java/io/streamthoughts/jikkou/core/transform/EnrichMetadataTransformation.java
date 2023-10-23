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
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.AcceptsResources;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Priority(HasPriority.HIGHEST_PRECEDENCE)
@Enabled
@AcceptsResources
public class EnrichMetadataTransformation implements ResourceTransformation<HasMetadata> {

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
