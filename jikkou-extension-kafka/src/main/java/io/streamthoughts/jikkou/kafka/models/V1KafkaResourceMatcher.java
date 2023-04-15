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
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;


/**
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("")
@JsonPropertyOrder({
    "type",
    "pattern",
    "patternType"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaResourceMatcher {

    /**
     * The resource type, i.e., TOPIC, GROUP.
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The resource type, i.e., TOPIC, GROUP.")
    private ResourceType type;
    /**
     * A literal name, a prefix, or a regex pattern for matching the resource.
     * (Required)
     * 
     */
    @JsonProperty("pattern")
    @JsonPropertyDescription("A literal name, a prefix, or a regex pattern for matching the resource.")
    private String pattern;
    /**
     * The pattern type, i.e., LITERAL, PREFIX, MATCH.
     * (Required)
     * 
     */
    @JsonProperty("patternType")
    @JsonPropertyDescription("The pattern type, i.e., LITERAL, PREFIX, MATCH.")
    private PatternType patternType = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaResourceMatcher() {
    }

    /**
     * 
     * @param pattern
     * @param patternType
     * @param type
     */
    @ConstructorProperties({
        "type",
        "pattern",
        "patternType"
    })
    public V1KafkaResourceMatcher(ResourceType type, String pattern, PatternType patternType) {
        super();
        this.type = type;
        this.pattern = pattern;
        this.patternType = patternType;
    }

    /**
     * The resource type, i.e., TOPIC, GROUP.
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ResourceType getType() {
        return type;
    }

    /**
     * A literal name, a prefix, or a regex pattern for matching the resource.
     * (Required)
     * 
     */
    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    /**
     * The pattern type, i.e., LITERAL, PREFIX, MATCH.
     * (Required)
     * 
     */
    @JsonProperty("patternType")
    public PatternType getPatternType() {
        return patternType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaResourceMatcher.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("pattern");
        sb.append('=');
        sb.append(((this.pattern == null)?"<null>":this.pattern));
        sb.append(',');
        sb.append("patternType");
        sb.append('=');
        sb.append(((this.patternType == null)?"<null>":this.patternType));
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
        result = ((result* 31)+((this.pattern == null)? 0 :this.pattern.hashCode()));
        result = ((result* 31)+((this.patternType == null)? 0 :this.patternType.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaResourceMatcher) == false) {
            return false;
        }
        V1KafkaResourceMatcher rhs = ((V1KafkaResourceMatcher) other);
        return ((((this.pattern == rhs.pattern)||((this.pattern!= null)&&this.pattern.equals(rhs.pattern)))&&((this.patternType == rhs.patternType)||((this.patternType!= null)&&this.patternType.equals(rhs.patternType))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))));
    }

}
