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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Singular;
import lombok.With;


/**
 * KafkaClusterSpec
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "topics",
    "quotas",
    "security"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaClusterSpec {

    @JsonProperty("topics")
    @Singular
    private List<V1KafkaTopicObject> topics = new ArrayList<V1KafkaTopicObject>();
    @JsonProperty("quotas")
    @Singular
    private List<V1KafkaQuotaObject> quotas = new ArrayList<V1KafkaQuotaObject>();
    /**
     * 
     */
    @JsonProperty("security")
    @JsonPropertyDescription("")
    private V1KafkaAuthorizationSpec security;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaClusterSpec() {
    }

    /**
     * 
     * @param security
     * @param quotas
     * @param topics
     */
    @ConstructorProperties({
        "topics",
        "quotas",
        "security"
    })
    public V1KafkaClusterSpec(List<V1KafkaTopicObject> topics, List<V1KafkaQuotaObject> quotas, V1KafkaAuthorizationSpec security) {
        super();
        this.topics = topics;
        this.quotas = quotas;
        this.security = security;
    }

    @JsonProperty("topics")
    public List<V1KafkaTopicObject> getTopics() {
        return topics;
    }

    @JsonProperty("quotas")
    public List<V1KafkaQuotaObject> getQuotas() {
        return quotas;
    }

    /**
     * 
     */
    @JsonProperty("security")
    public V1KafkaAuthorizationSpec getSecurity() {
        return security;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaClusterSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("topics");
        sb.append('=');
        sb.append(((this.topics == null)?"<null>":this.topics));
        sb.append(',');
        sb.append("quotas");
        sb.append('=');
        sb.append(((this.quotas == null)?"<null>":this.quotas));
        sb.append(',');
        sb.append("security");
        sb.append('=');
        sb.append(((this.security == null)?"<null>":this.security));
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
        result = ((result* 31)+((this.security == null)? 0 :this.security.hashCode()));
        result = ((result* 31)+((this.quotas == null)? 0 :this.quotas.hashCode()));
        result = ((result* 31)+((this.topics == null)? 0 :this.topics.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaClusterSpec) == false) {
            return false;
        }
        V1KafkaClusterSpec rhs = ((V1KafkaClusterSpec) other);
        return ((((this.security == rhs.security)||((this.security!= null)&&this.security.equals(rhs.security)))&&((this.quotas == rhs.quotas)||((this.quotas!= null)&&this.quotas.equals(rhs.quotas))))&&((this.topics == rhs.topics)||((this.topics!= null)&&this.topics.equals(rhs.topics))));
    }

}
