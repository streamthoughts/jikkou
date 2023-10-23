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
package io.streamthoughts.jikkou.extension.aiven.converter;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.resource.converter.AbstractResourceListConverter;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class V1KafkaQuotaListConverter extends AbstractResourceListConverter<V1KafkaQuotaList, V1KafkaQuota> {


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<V1KafkaQuotaList> convertTo(@NotNull List<V1KafkaQuota> resources) {
        List<V1KafkaQuota> objects = resources
                .stream()
                .map(item -> item
                        .withKind(null)
                        .withApiVersion(null)
                )
                .toList();

        return List.of(
                V1KafkaQuotaList.builder()
                        .withMetadata(ObjectMeta.builder()
                                .withAnnotation(CoreAnnotations.JIKKOU_IO_ITEMS_COUNT, objects.size())
                                .build()
                        )
                        .withItems(objects)
                        .build()
        );
    }
}
