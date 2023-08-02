/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.converter;

import io.streamthoughts.jikkou.api.model.HasSpec;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractResourceListConverter<T extends ResourceListObject<E>, E extends HasSpec<?>>
        implements ResourceConverter<T, E> {


    /** {@inheritDoc} **/
    @Override
    public @NotNull List<E> convertFrom(@NotNull List<T> resources) {
        return resources.stream().flatMap(this::map).toList();
    }

    /** {@inheritDoc} **/
    @Override
    public abstract @NotNull List<T> convertTo(@NotNull List<E> resources);

    public Stream<E> map(ResourceListObject<E> resource) {
        if (resource == null || resource.getItems() == null) {
            return Stream.empty();
        }

        ObjectMeta metadata = resource.getMetadata();
        Map<String, Object> listLabels = metadata != null ? metadata.getLabels() : Collections.emptyMap();
        Map<String, Object> listAnnotations = metadata != null ? metadata.getAnnotations() : Collections.emptyMap();

        return resource
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

                    @SuppressWarnings("unchecked")
                    E updated = (E) item.withMetadata(objectMeta);
                    return updated;
                });
    }
}
