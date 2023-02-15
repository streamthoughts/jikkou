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
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "pattern",
    "pattern_type",
    "type"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaAccessResourceMatcher {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("pattern_type")
    private PatternType patternType = null;
    @JsonProperty("type")
    private ResourceType type;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaAccessResourceMatcher() {
    }

    /**
     * 
     * @param pattern
     * @param patternType
     * @param type
     */
    @ConstructorProperties({
        "pattern",
        "patternType",
        "type"
    })
    public V1KafkaAccessResourceMatcher(String pattern, PatternType patternType, ResourceType type) {
        super();
        this.pattern = pattern;
        this.patternType = patternType;
        this.type = type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    @JsonProperty("pattern_type")
    public PatternType getPatternType() {
        return patternType;
    }

    @JsonProperty("type")
    public ResourceType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaAccessResourceMatcher.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("pattern");
        sb.append('=');
        sb.append(((this.pattern == null)?"<null>":this.pattern));
        sb.append(',');
        sb.append("patternType");
        sb.append('=');
        sb.append(((this.patternType == null)?"<null>":this.patternType));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
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
        if ((other instanceof V1KafkaAccessResourceMatcher) == false) {
            return false;
        }
        V1KafkaAccessResourceMatcher rhs = ((V1KafkaAccessResourceMatcher) other);
        return ((((this.pattern == rhs.pattern)||((this.pattern!= null)&&this.pattern.equals(rhs.pattern)))&&((this.patternType == rhs.patternType)||((this.patternType!= null)&&this.patternType.equals(rhs.patternType))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))));
    }

}
