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
package io.streamthoughts.jikkou.kafka.connect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "connectorStatus"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaConnectorStatus {

    /**
     * The connector status, as reported by the Kafka Connect REST API.
     * 
     */
    @JsonProperty("connectorStatus")
    @JsonPropertyDescription("The connector status, as reported by the Kafka Connect REST API.")
    private Object connectorStatus;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaConnectorStatus() {
    }

    /**
     * 
     * @param connectorStatus
     */
    @ConstructorProperties({
        "connectorStatus"
    })
    public V1KafkaConnectorStatus(Object connectorStatus) {
        super();
        this.connectorStatus = connectorStatus;
    }

    /**
     * The connector status, as reported by the Kafka Connect REST API.
     * 
     */
    @JsonProperty("connectorStatus")
    public Object getConnectorStatus() {
        return connectorStatus;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaConnectorStatus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("connectorStatus");
        sb.append('=');
        sb.append(((this.connectorStatus == null)?"<null>":this.connectorStatus));
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
        result = ((result* 31)+((this.connectorStatus == null)? 0 :this.connectorStatus.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaConnectorStatus) == false) {
            return false;
        }
        V1KafkaConnectorStatus rhs = ((V1KafkaConnectorStatus) other);
        return ((this.connectorStatus == rhs.connectorStatus)||((this.connectorStatus!= null)&&this.connectorStatus.equals(rhs.connectorStatus)));
    }

}
