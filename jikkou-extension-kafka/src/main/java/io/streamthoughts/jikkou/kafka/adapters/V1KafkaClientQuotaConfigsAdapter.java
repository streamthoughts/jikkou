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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaConfigs;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

public final class V1KafkaClientQuotaConfigsAdapter {

    public static final String PRODUCER_BYTE_RATE = "producer_byte_rate";
    public static final String CONSUMER_BYTE_RATE = "consumer_byte_rate";
    public static final String REQUEST_PERCENTAGE = "request_percentage";

    public static Map<String, Double> toClientQuotaConfigs(V1KafkaClientQuotaConfigs limits) {
        final Map<String, Double> m = new HashMap<>();
        limits.getConsumerByteRate().ifPresent(it -> m.put(CONSUMER_BYTE_RATE, it));
        limits.getProducerByteRate().ifPresent(it -> m.put(PRODUCER_BYTE_RATE, it));
        limits.getRequestPercentage().ifPresent(it -> m.put(REQUEST_PERCENTAGE, it));
        return m;
    }

    public static V1KafkaClientQuotaConfigs toClientQuotaConfigs(Map<String, Double> limits) {
        Double producerByteRate = limits.get(PRODUCER_BYTE_RATE);
        Double consumerByteRate = limits.get(CONSUMER_BYTE_RATE);
        Double requestPercentage = limits.get(REQUEST_PERCENTAGE);
        return V1KafkaClientQuotaConfigs
                .builder()
                .withProducerByteRate(producerByteRate == null ? OptionalDouble.empty() : OptionalDouble.of(producerByteRate))
                .withConsumerByteRate(consumerByteRate == null ? OptionalDouble.empty() : OptionalDouble.of(consumerByteRate))
                .withRequestPercentage(requestPercentage == null ? OptionalDouble.empty() : OptionalDouble.of(requestPercentage))
                .build();
    }
}
