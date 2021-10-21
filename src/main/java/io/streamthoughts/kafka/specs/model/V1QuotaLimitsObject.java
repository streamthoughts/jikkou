/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

public class V1QuotaLimitsObject {

    public static final String PRODUCER_BYTE_RATE = "producer_byte_rate";
    public static final String CONSUMER_BYTE_RATE = "consumer_byte_rate";
    public static final String REQUEST_BYTE_RATE = "request_byte_rate";

    private final OptionalDouble producerByteRate;
    private final OptionalDouble consumerByteRate;
    private final OptionalDouble requestPercentage;

    public V1QuotaLimitsObject(@NotNull final Map<String, Double> configs) {
        this(
              configs.get(PRODUCER_BYTE_RATE),
              configs.get(CONSUMER_BYTE_RATE),
              configs.get(REQUEST_BYTE_RATE)
        );
    }

    /**
     * Creates a new {@link V1QuotaLimitsObject} instance.
     *
     * @param producerByteRate  the quota in bytes for restricting data production.
     * @param consumerByteRate  the quota in bytes for restricting data consumption.
     * @param requestPercentage the quota in percentage (%) of total requests.
     */
    @JsonCreator
    public V1QuotaLimitsObject(@JsonProperty(PRODUCER_BYTE_RATE) @Nullable final Double producerByteRate,
                               @JsonProperty(CONSUMER_BYTE_RATE) @Nullable final Double consumerByteRate,
                               @JsonProperty(REQUEST_BYTE_RATE) @Nullable final Double requestPercentage) {
        this.producerByteRate = producerByteRate == null ?
                OptionalDouble.empty() : OptionalDouble.of(producerByteRate);
        this.consumerByteRate = consumerByteRate == null ?
                OptionalDouble.empty() : OptionalDouble.of(consumerByteRate);
        this.requestPercentage = requestPercentage == null ?
                OptionalDouble.empty() : OptionalDouble.of(requestPercentage);
    }

    @JsonProperty(PRODUCER_BYTE_RATE)
    public OptionalDouble getProducerByteRate() {
        return producerByteRate;
    }

    @JsonProperty(CONSUMER_BYTE_RATE)
    public OptionalDouble getConsumerByteRate() {
        return consumerByteRate;
    }

    @JsonProperty(REQUEST_BYTE_RATE)
    public OptionalDouble getRequestPercentage() {
        return requestPercentage;
    }

    public Map<String, Double> toMapDouble() {
        final Map<String, Double> m = new HashMap<>();
        getConsumerByteRate().ifPresent(it -> m.put(CONSUMER_BYTE_RATE, it));
        getProducerByteRate().ifPresent(it -> m.put(PRODUCER_BYTE_RATE, it));
        getRequestPercentage().ifPresent(it -> m.put(REQUEST_BYTE_RATE, it));
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1QuotaLimitsObject)) return false;
        V1QuotaLimitsObject that = (V1QuotaLimitsObject) o;
        return Objects.equals(producerByteRate, that.producerByteRate) &&
                Objects.equals(consumerByteRate, that.consumerByteRate) &&
                Objects.equals(requestPercentage, that.requestPercentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producerByteRate, consumerByteRate, requestPercentage);
    }

    @Override
    public String toString() {
        return "V1QuotaConfigsObject{" +
                "producerByteRate=" + producerByteRate +
                ", consumerByteRate=" + consumerByteRate +
                ", requestPercentage=" + requestPercentage +
                '}';
    }
}
