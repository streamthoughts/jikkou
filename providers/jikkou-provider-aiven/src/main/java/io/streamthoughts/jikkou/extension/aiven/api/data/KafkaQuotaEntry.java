/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Reflectable
public final class KafkaQuotaEntry {


    private final String clientId;

    private final String user;

    private final Double consumerByteRate;

    private final Double producerByteRate;

    private final Double requestPercentage;

    @JsonCreator
    public KafkaQuotaEntry(@NotNull @JsonProperty("client-id") String clientId,
                           @NotNull @JsonProperty("user") String user,
                           @Nullable @JsonProperty("consumer_byte_rate") Double consumerByteRate,
                           @Nullable @JsonProperty("producer_byte_rate") Double producerByteRate,
                           @Nullable @JsonProperty("request_percentage") Double requestPercentage) {
        this.clientId = clientId;
        this.user = user;
        this.consumerByteRate = consumerByteRate;
        this.producerByteRate = producerByteRate;
        this.requestPercentage = requestPercentage;
    }

    @JsonProperty("client-id")
    public String clientId() {
        return clientId;
    }

    @JsonProperty("user")
    public String user() {
        return user;
    }

    @JsonProperty("consumer_byte_rate")
    public Double consumerByteRate() {
        return consumerByteRate;
    }

    @JsonProperty("producer_byte_rate")
    public Double producerByteRate() {
        return producerByteRate;
    }

    @JsonProperty("request_percentage")
    public Double requestPercentage() {
        return requestPercentage;
    }

    /** {@inheritDoc } **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaQuotaEntry that = (KafkaQuotaEntry) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(user, that.user) && Objects.equals(consumerByteRate, that.consumerByteRate) && Objects.equals(producerByteRate, that.producerByteRate) && Objects.equals(requestPercentage, that.requestPercentage);
    }

    /** {@inheritDoc } **/
    @Override
    public int hashCode() {
        return Objects.hash(clientId, user, consumerByteRate, producerByteRate, requestPercentage);
    }

    /** {@inheritDoc } **/
    @Override
    public String toString() {
        return "KafkaQuotaEntry{" +
                "clientId='" + clientId + '\'' +
                ", user='" + user + '\'' +
                ", consumerByteRate=" + consumerByteRate +
                ", producerByteRate=" + producerByteRate +
                ", requestPercentage=" + requestPercentage +
                '}';
    }
}
