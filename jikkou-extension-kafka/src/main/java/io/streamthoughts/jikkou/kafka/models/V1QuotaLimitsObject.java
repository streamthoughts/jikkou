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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.beans.ConstructorProperties;
import java.util.OptionalDouble;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "producer_byte_rate",
    "consumer_byte_rate",
    "request_percentage"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1QuotaLimitsObject {

    /**
     * The quota in bytes for restricting data production.
     * 
     */
    @JsonProperty("producer_byte_rate")
    @JsonPropertyDescription("The quota in bytes for restricting data production.")
    private OptionalDouble producerByteRate;
    /**
     * The quota in bytes for restricting data consumption.
     * 
     */
    @JsonProperty("consumer_byte_rate")
    @JsonPropertyDescription("The quota in bytes for restricting data consumption.")
    private OptionalDouble consumerByteRate;
    /**
     * The quota in percentage (%) of total requests.
     * 
     */
    @JsonProperty("request_percentage")
    @JsonPropertyDescription("The quota in percentage (%) of total requests.")
    private OptionalDouble requestPercentage;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1QuotaLimitsObject() {
    }

    /**
     * 
     * @param producerByteRate
     * @param consumerByteRate
     * @param requestPercentage
     */
    @ConstructorProperties({
        "producerByteRate",
        "consumerByteRate",
        "requestPercentage"
    })
    public V1QuotaLimitsObject(OptionalDouble producerByteRate, OptionalDouble consumerByteRate, OptionalDouble requestPercentage) {
        super();
        this.producerByteRate = producerByteRate;
        this.consumerByteRate = consumerByteRate;
        this.requestPercentage = requestPercentage;
    }

    /**
     * The quota in bytes for restricting data production.
     * 
     */
    @JsonProperty("producer_byte_rate")
    public OptionalDouble getProducerByteRate() {
        return producerByteRate;
    }

    /**
     * The quota in bytes for restricting data consumption.
     * 
     */
    @JsonProperty("consumer_byte_rate")
    public OptionalDouble getConsumerByteRate() {
        return consumerByteRate;
    }

    /**
     * The quota in percentage (%) of total requests.
     * 
     */
    @JsonProperty("request_percentage")
    public OptionalDouble getRequestPercentage() {
        return requestPercentage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1QuotaLimitsObject.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("producerByteRate");
        sb.append('=');
        sb.append(((this.producerByteRate == null)?"<null>":this.producerByteRate));
        sb.append(',');
        sb.append("consumerByteRate");
        sb.append('=');
        sb.append(((this.consumerByteRate == null)?"<null>":this.consumerByteRate));
        sb.append(',');
        sb.append("requestPercentage");
        sb.append('=');
        sb.append(((this.requestPercentage == null)?"<null>":this.requestPercentage));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.producerByteRate == null)? 0 :this.producerByteRate.hashCode()));
        result = ((result* 31)+((this.consumerByteRate == null)? 0 :this.consumerByteRate.hashCode()));
        result = ((result* 31)+((this.requestPercentage == null)? 0 :this.requestPercentage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1QuotaLimitsObject) == false) {
            return false;
        }
        V1QuotaLimitsObject rhs = ((V1QuotaLimitsObject) other);
        return ((((this.producerByteRate == rhs.producerByteRate)||((this.producerByteRate!= null)&&this.producerByteRate.equals(rhs.producerByteRate)))&&((this.consumerByteRate == rhs.consumerByteRate)||((this.consumerByteRate!= null)&&this.consumerByteRate.equals(rhs.consumerByteRate))))&&((this.requestPercentage == rhs.requestPercentage)||((this.requestPercentage!= null)&&this.requestPercentage.equals(rhs.requestPercentage))));
    }

}
