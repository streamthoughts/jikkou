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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "brokers"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaBrokersSpec {

    @JsonProperty("brokers")
    @Singular
    private List<V1KafkaBrokerObject> brokers = new ArrayList<V1KafkaBrokerObject>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaBrokersSpec() {
    }

    /**
     * 
     * @param brokers
     */
    @ConstructorProperties({
        "brokers"
    })
    public V1KafkaBrokersSpec(List<V1KafkaBrokerObject> brokers) {
        super();
        this.brokers = brokers;
    }

    @JsonProperty("brokers")
    public List<V1KafkaBrokerObject> getBrokers() {
        return brokers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaBrokersSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("brokers");
        sb.append('=');
        sb.append(((this.brokers == null)?"<null>":this.brokers));
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
        result = ((result* 31)+((this.brokers == null)? 0 :this.brokers.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaBrokersSpec) == false) {
            return false;
        }
        V1KafkaBrokersSpec rhs = ((V1KafkaBrokersSpec) other);
        return ((this.brokers == rhs.brokers)||((this.brokers!= null)&&this.brokers.equals(rhs.brokers)));
    }

}
