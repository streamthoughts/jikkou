/*
 * Copyright 2024 The original authors
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
package io.streamthoughts.jikkou.extension.aiven.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "clientId",
    "user",
    "consumerByteRate",
    "producerByteRate",
    "requestPercentage"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaQuotaSpec {

    /**
     * client-id
     * (Required)
     * 
     */
    @JsonProperty("clientId")
    @JsonPropertyDescription("client-id")
    @Builder.Default
    private String clientId = "default";
    /**
     * Username
     * (Required)
     * 
     */
    @JsonProperty("user")
    @JsonPropertyDescription("Username")
    @Builder.Default
    private String user = "default";
    /**
     * The quota in bytes for restricting data consumption
     * 
     */
    @JsonProperty("consumerByteRate")
    @JsonPropertyDescription("The quota in bytes for restricting data consumption")
    private Double consumerByteRate;
    /**
     * The quota in bytes for restricting data production
     * 
     */
    @JsonProperty("producerByteRate")
    @JsonPropertyDescription("The quota in bytes for restricting data production")
    private Double producerByteRate;
    /**
     * The quota in percentage (%) of CPU throttling
     * 
     */
    @JsonProperty("requestPercentage")
    @JsonPropertyDescription("The quota in percentage (%) of CPU throttling")
    private Double requestPercentage;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaQuotaSpec() {
    }

    /**
     * 
     * @param consumerByteRate
     * @param producerByteRate
     * @param requestPercentage
     * @param clientId
     * @param user
     */
    @ConstructorProperties({
        "clientId",
        "user",
        "consumerByteRate",
        "producerByteRate",
        "requestPercentage"
    })
    public V1KafkaQuotaSpec(String clientId, String user, Double consumerByteRate, Double producerByteRate, Double requestPercentage) {
        super();
        this.clientId = clientId;
        this.user = user;
        this.consumerByteRate = consumerByteRate;
        this.producerByteRate = producerByteRate;
        this.requestPercentage = requestPercentage;
    }

    /**
     * client-id
     * (Required)
     * 
     */
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * Username
     * (Required)
     * 
     */
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    /**
     * The quota in bytes for restricting data consumption
     * 
     */
    @JsonProperty("consumerByteRate")
    public Double getConsumerByteRate() {
        return consumerByteRate;
    }

    /**
     * The quota in bytes for restricting data production
     * 
     */
    @JsonProperty("producerByteRate")
    public Double getProducerByteRate() {
        return producerByteRate;
    }

    /**
     * The quota in percentage (%) of CPU throttling
     * 
     */
    @JsonProperty("requestPercentage")
    public Double getRequestPercentage() {
        return requestPercentage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaQuotaSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("clientId");
        sb.append('=');
        sb.append(((this.clientId == null)?"<null>":this.clientId));
        sb.append(',');
        sb.append("user");
        sb.append('=');
        sb.append(((this.user == null)?"<null>":this.user));
        sb.append(',');
        sb.append("consumerByteRate");
        sb.append('=');
        sb.append(((this.consumerByteRate == null)?"<null>":this.consumerByteRate));
        sb.append(',');
        sb.append("producerByteRate");
        sb.append('=');
        sb.append(((this.producerByteRate == null)?"<null>":this.producerByteRate));
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
        result = ((result* 31)+((this.consumerByteRate == null)? 0 :this.consumerByteRate.hashCode()));
        result = ((result* 31)+((this.producerByteRate == null)? 0 :this.producerByteRate.hashCode()));
        result = ((result* 31)+((this.requestPercentage == null)? 0 :this.requestPercentage.hashCode()));
        result = ((result* 31)+((this.clientId == null)? 0 :this.clientId.hashCode()));
        result = ((result* 31)+((this.user == null)? 0 :this.user.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaQuotaSpec) == false) {
            return false;
        }
        V1KafkaQuotaSpec rhs = ((V1KafkaQuotaSpec) other);
        return ((((((this.consumerByteRate == rhs.consumerByteRate)||((this.consumerByteRate!= null)&&this.consumerByteRate.equals(rhs.consumerByteRate)))&&((this.producerByteRate == rhs.producerByteRate)||((this.producerByteRate!= null)&&this.producerByteRate.equals(rhs.producerByteRate))))&&((this.requestPercentage == rhs.requestPercentage)||((this.requestPercentage!= null)&&this.requestPercentage.equals(rhs.requestPercentage))))&&((this.clientId == rhs.clientId)||((this.clientId!= null)&&this.clientId.equals(rhs.clientId))))&&((this.user == rhs.user)||((this.user!= null)&&this.user.equals(rhs.user))));
    }

}
