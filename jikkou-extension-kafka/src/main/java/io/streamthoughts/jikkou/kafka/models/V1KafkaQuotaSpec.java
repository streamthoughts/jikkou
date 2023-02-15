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
    "quotas"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaQuotaSpec {

    @JsonProperty("quotas")
    @Singular
    private List<V1KafkaQuotaObject> quotas = new ArrayList<V1KafkaQuotaObject>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaQuotaSpec() {
    }

    /**
     * 
     * @param quotas
     */
    @ConstructorProperties({
        "quotas"
    })
    public V1KafkaQuotaSpec(List<V1KafkaQuotaObject> quotas) {
        super();
        this.quotas = quotas;
    }

    @JsonProperty("quotas")
    public List<V1KafkaQuotaObject> getQuotas() {
        return quotas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaQuotaSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("quotas");
        sb.append('=');
        sb.append(((this.quotas == null)?"<null>":this.quotas));
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
        result = ((result* 31)+((this.quotas == null)? 0 :this.quotas.hashCode()));
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
        return ((this.quotas == rhs.quotas)||((this.quotas!= null)&&this.quotas.equals(rhs.quotas)));
    }

}
