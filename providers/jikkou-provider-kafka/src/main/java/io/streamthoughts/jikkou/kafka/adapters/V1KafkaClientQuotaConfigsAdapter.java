/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.adapters;

import static java.util.Optional.ofNullable;

import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaConfigs;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

public final class V1KafkaClientQuotaConfigsAdapter {

    public static final String PRODUCER_BYTE_RATE = "producer_byte_rate";
    public static final String CONSUMER_BYTE_RATE = "consumer_byte_rate";
    public static final String REQUEST_PERCENTAGE = "request_percentage";
    private static final OptionalDouble EMPTY = OptionalDouble.empty();

    public static Map<String, Double> toClientQuotaConfigs(KafkaClientQuotaConfigs limits) {
        final Map<String, Double> result = new HashMap<>();
        if (limits != null) {
            limits.getConsumerByteRate().ifPresent(it -> result.put(CONSUMER_BYTE_RATE, it));
            limits.getProducerByteRate().ifPresent(it -> result.put(PRODUCER_BYTE_RATE, it));
            limits.getRequestPercentage().ifPresent(it -> result.put(REQUEST_PERCENTAGE, it));
        }
        return result;
    }

    public static KafkaClientQuotaConfigs toClientQuotaConfigs(Map<String, Double> limits) {
        if (limits == null) limits = new HashMap<>();
        return KafkaClientQuotaConfigs
                .builder()
                .withProducerByteRate(ofNullable(limits.get(PRODUCER_BYTE_RATE)).map(OptionalDouble::of).orElse(EMPTY))
                .withConsumerByteRate(ofNullable(limits.get(CONSUMER_BYTE_RATE)).map(OptionalDouble::of).orElse(EMPTY))
                .withRequestPercentage(ofNullable(limits.get(REQUEST_PERCENTAGE)).map(OptionalDouble::of).orElse(EMPTY))
                .build();
    }
}
