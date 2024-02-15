/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import lombok.Builder;
import lombok.With;


/**
 * Client Quota Limits
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Client quota configuration")
@JsonPropertyOrder({
    "producerByteRate",
    "consumerByteRate",
    "requestPercentage"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Reflectable
public class KafkaClientQuotaConfigs {

    /**
     * The quota in bytes for restricting data production.
     */
    @JsonProperty("producerByteRate")
    @JsonPropertyDescription("The quota in bytes for restricting data production.")
    private OptionalDouble producerByteRate;
    /**
     * The quota in bytes for restricting data consumption.
     */
    @JsonProperty("consumerByteRate")
    @JsonPropertyDescription("The quota in bytes for restricting data consumption.")
    private OptionalDouble consumerByteRate ;

    /**
     * The quota in percentage (%) of total requests.
     */
    @JsonProperty("requestPercentage")
    @JsonPropertyDescription("The quota in percentage (%) of total requests.")
    private OptionalDouble requestPercentage;

    /**
     * No args constructor for use in serialization
     * 
     */
    public KafkaClientQuotaConfigs() {
    }

    /**
     * Creates a new {@link KafkaClientQuotaConfigs} instance.
     *
     * @param producerByteRate {@link #producerByteRate}.
     * @param consumerByteRate {@link #consumerByteRate}.
     * @param requestPercentage {@link #requestPercentage}.
     */
    @ConstructorProperties({
        "producerByteRate",
        "consumerByteRate",
        "requestPercentage"
    })
    public KafkaClientQuotaConfigs(OptionalDouble producerByteRate,
                                   OptionalDouble consumerByteRate,
                                   OptionalDouble requestPercentage) {
        super();
        this.producerByteRate = Optional.ofNullable(producerByteRate).orElse(OptionalDouble.empty());
        this.consumerByteRate = Optional.ofNullable(consumerByteRate).orElse(OptionalDouble.empty());
        this.requestPercentage = Optional.ofNullable(requestPercentage).orElse(OptionalDouble.empty());
    }

    /**
     * The quota in bytes for restricting data production.
     * 
     */
    @JsonProperty("producerByteRate")
    public OptionalDouble getProducerByteRate() {
        return producerByteRate;
    }

    /**
     * The quota in bytes for restricting data consumption.
     * 
     */
    @JsonProperty("consumerByteRate")
    public OptionalDouble getConsumerByteRate() {
        return consumerByteRate;
    }

    /**
     * The quota in percentage (%) of total requests.
     * 
     */
    @JsonProperty("requestPercentage")
    public OptionalDouble getRequestPercentage() {
        return requestPercentage;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaClientQuotaConfigs that = (KafkaClientQuotaConfigs) o;
        return Objects.equals(producerByteRate, that.producerByteRate) && Objects.equals(consumerByteRate, that.consumerByteRate) && Objects.equals(requestPercentage, that.requestPercentage);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(producerByteRate, consumerByteRate, requestPercentage);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "KafkaClientQuotaConfigs{" +
                "producerByteRate=" + producerByteRate +
                ", consumerByteRate=" + consumerByteRate +
                ", requestPercentage=" + requestPercentage +
                '}';
    }
}
