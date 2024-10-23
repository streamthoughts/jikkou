/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Enabled
public final class ResourceListConverter implements Converter<HasMetadata, HasMetadata> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull List<HasMetadata> apply(@NotNull HasMetadata resource) {

        if (resource instanceof ResourceList<?> list) {
            if (list.getItems() == null) {
                return Collections.emptyList();
            }
            ObjectMeta metadata = resource.getMetadata();
            Map<String, Object> listLabels = metadata != null ? metadata.getLabels() : Collections.emptyMap();
            Map<String, Object> listAnnotations = metadata != null ? metadata.getAnnotations() : Collections.emptyMap();
            return list
                    .getItems()
                    .stream()
                    .map(item -> {
                        var labels = new HashMap<>(listLabels);
                        var annotations = new HashMap<>(listAnnotations);

                        ObjectMeta itemObjectMeta = item.getMetadata();
                        if (itemObjectMeta != null) {
                            labels.putAll(itemObjectMeta.getLabels());
                            annotations.putAll(itemObjectMeta.getAnnotations());
                        } else {
                            itemObjectMeta = new ObjectMeta();
                        }

                        ObjectMeta objectMeta = itemObjectMeta
                                .toBuilder()
                                .withLabels(labels)
                                .withAnnotations(annotations)
                                .build();

                        return (HasSpec<?>) item.withMetadata(objectMeta);
                    })
                    .collect(Collectors.toList());
        }

        return List.of(resource);
    }
}
