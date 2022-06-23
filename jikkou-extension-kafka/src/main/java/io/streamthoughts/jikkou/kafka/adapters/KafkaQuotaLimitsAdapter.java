/*
 * Copyright 2022 StreamThoughts.
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

import io.streamthoughts.jikkou.kafka.models.V1QuotaLimitsObject;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KafkaQuotaLimitsAdapter {

    public static final String PRODUCER_BYTE_RATE = "producer_byte_rate";
    public static final String CONSUMER_BYTE_RATE = "consumer_byte_rate";
    public static final String REQUEST_BYTE_RATE = "request_byte_rate";

    private final V1QuotaLimitsObject object;

    /**
     * Creates a new {@link KafkaQuotaLimitsAdapter} instance.
     *
     * @param limits the quota limits
     */
    public KafkaQuotaLimitsAdapter(@Nullable final Map<String, Double> limits) {
        this(
                limits.get(PRODUCER_BYTE_RATE),
                limits.get(CONSUMER_BYTE_RATE),
                limits.get(REQUEST_BYTE_RATE)
        );
    }

    /**
     * Creates a new {@link KafkaQuotaLimitsAdapter} instance.
     *
     * @param producerByteRate  the quota in bytes for restricting data production.
     * @param consumerByteRate  the quota in bytes for restricting data consumption.
     * @param requestPercentage the quota in percentage (%) of total requests.
     */
    public KafkaQuotaLimitsAdapter(@Nullable final Double producerByteRate,
                                   @Nullable final Double consumerByteRate,
                                   @Nullable final Double requestPercentage) {
        this(new V1QuotaLimitsObject(
                producerByteRate == null ? OptionalDouble.empty() : OptionalDouble.of(producerByteRate),
                consumerByteRate == null ? OptionalDouble.empty() : OptionalDouble.of(consumerByteRate),
                requestPercentage == null ? OptionalDouble.empty() : OptionalDouble.of(requestPercentage)
        ));
    }

    /**
     * Creates a new {@link KafkaQuotaLimitsAdapter} instance.
     *
     * @param limitsObject the {@link V1QuotaLimitsObject}.
     */
    public KafkaQuotaLimitsAdapter(final @NotNull V1QuotaLimitsObject limitsObject) {
        this.object = limitsObject;
    }

    public Map<String, Double> toMapDouble() {
        final Map<String, Double> m = new HashMap<>();
        object.getConsumerByteRate().ifPresent(it -> m.put(CONSUMER_BYTE_RATE, it));
        object.getProducerByteRate().ifPresent(it -> m.put(PRODUCER_BYTE_RATE, it));
        object.getRequestPercentage().ifPresent(it -> m.put(REQUEST_BYTE_RATE, it));
        return m;
    }

    public V1QuotaLimitsObject toV1QuotaLimitsObject() {
        return object;
    }

}
