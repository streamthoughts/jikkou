/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.util.Optional;

/**
 *
 */
public final class KafkaQuotaAdapter {

    public static final String DEFAULT = "default";

    public static KafkaQuotaEntry map(final  V1KafkaQuota entry) {
        if (entry == null) return null;
        return new KafkaQuotaEntry(
                Optional.ofNullable(entry.getSpec().getClientId()).orElse(DEFAULT),
                Optional.ofNullable(entry.getSpec().getUser()).orElse(DEFAULT),
                entry.getSpec().getConsumerByteRate(),
                entry.getSpec().getProducerByteRate(),
                entry.getSpec().getRequestPercentage()
        );
    }

    public static V1KafkaQuota map(final KafkaQuotaEntry entry) {
        if (entry == null) return null;
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
