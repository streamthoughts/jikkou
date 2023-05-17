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
import io.streamthoughts.jikkou.api.model.annotations.Description;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaConfigs;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;


/**
 * Client quota specification
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Client quota specification")
@JsonPropertyOrder({
    "type",
    "entity",
    "configs"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaClientQuotaSpec {

    @JsonProperty("type")
    private KafkaClientQuotaType type;
    /**
     * A secure logical group of clients that share both user principal and client ID.
     * 
     */
    @JsonProperty("entity")
    @JsonPropertyDescription("A secure logical group of clients that share both user principal and client ID.")
    private V1KafkaClientQuotaEntity entity;
    @JsonProperty("configs")
    private KafkaClientQuotaConfigs configs;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaClientQuotaSpec() {
    }

    /**
     * 
     * @param configs
     * @param type
     * @param entity
     */
    @ConstructorProperties({
        "type",
        "entity",
        "configs"
    })
    public V1KafkaClientQuotaSpec(KafkaClientQuotaType type, V1KafkaClientQuotaEntity entity, KafkaClientQuotaConfigs configs) {
        super();
        this.type = type;
        this.entity = entity;
        this.configs = configs;
    }

    @JsonProperty("type")
    public KafkaClientQuotaType getType() {
        return type;
    }

    /**
     * A secure logical group of clients that share both user principal and client ID.
     * 
     */
    @JsonProperty("entity")
    public V1KafkaClientQuotaEntity getEntity() {
        return entity;
    }

    @JsonProperty("configs")
    public KafkaClientQuotaConfigs getConfigs() {
        return configs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaClientQuotaSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("entity");
        sb.append('=');
        sb.append(((this.entity == null)?"<null>":this.entity));
        sb.append(',');
        sb.append("configs");
        sb.append('=');
        sb.append(((this.configs == null)?"<null>":this.configs));
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
        result = ((result* 31)+((this.configs == null)? 0 :this.configs.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.entity == null)? 0 :this.entity.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaClientQuotaSpec) == false) {
            return false;
        }
        V1KafkaClientQuotaSpec rhs = ((V1KafkaClientQuotaSpec) other);
        return ((((this.configs == rhs.configs)||((this.configs!= null)&&this.configs.equals(rhs.configs)))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.entity == rhs.entity)||((this.entity!= null)&&this.entity.equals(rhs.entity))));
    }

}
