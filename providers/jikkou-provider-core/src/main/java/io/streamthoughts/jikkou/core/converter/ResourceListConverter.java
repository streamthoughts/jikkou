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
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
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

        if (resource instanceof ResourceListObject<?> list) {
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
