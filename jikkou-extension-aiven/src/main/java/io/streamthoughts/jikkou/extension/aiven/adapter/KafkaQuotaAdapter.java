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
package io.streamthoughts.jikkou.extension.aiven.adapter;

import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class KafkaQuotaAdapter {

    public static final String DEFAULT = "default";

    public static KafkaQuotaEntry map(final @NotNull V1KafkaQuota entry) {
        return new KafkaQuotaEntry(
                Optional.ofNullable(entry.getSpec().getClientId()).orElse(DEFAULT),
                Optional.ofNullable(entry.getSpec().getUser()).orElse(DEFAULT),
                entry.getSpec().getConsumerByteRate(),
                entry.getSpec().getProducerByteRate(),
                entry.getSpec().getRequestPercentage()
        );
    }

    public static V1KafkaQuota map(final @NotNull KafkaQuotaEntry entry) {
        return V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withClientId(entry.clientId())
                        .withUser(entry.user())
                        .withConsumerByteRate(entry.consumerByteRate())
                        .withProducerByteRate(entry.producerByteRate())
                        .withRequestPercentage(entry.requestPercentage())
                        .build()
                )
                .build();
    }

    private KafkaQuotaAdapter() {}
}
