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
package io.streamthoughts.jikkou.extension.aiven.converter;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.converter.AbstractResourceListConverter;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntryList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class V1SchemaRegistryAclEntryListConverter extends AbstractResourceListConverter<V1SchemaRegistryAclEntryList, V1SchemaRegistryAclEntry> {


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<V1SchemaRegistryAclEntryList> convertTo(@NotNull List<V1SchemaRegistryAclEntry> resources) {
        List<V1SchemaRegistryAclEntry> objects = resources
                .stream()
                .map(item -> item
                        .withKind(null)
                        .withApiVersion(null)
                )
                .toList();

        return List.of(
                new V1SchemaRegistryAclEntryList()
                        .withMetadata(ObjectMeta.builder()
                                .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_ITEMS_COUNT, objects.size())
                                .build()
                        )
                        .withItems(objects)
                        .toBuilder()
                        .build()
        );
    }

    @Override
    public V1SchemaRegistryAclEntry updateMetadata(V1SchemaRegistryAclEntry resource,
                                                   ObjectMeta objectMeta) {
        return resource.withMetadata(objectMeta);
    }

}
