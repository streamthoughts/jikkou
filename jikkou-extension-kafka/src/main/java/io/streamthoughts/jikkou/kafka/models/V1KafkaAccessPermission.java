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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.kafka.model.AccessOperationPolicy;
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
    "resource",
    "allow_operations"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Generated("jsonschema2pojo")
public class V1KafkaAccessPermission {

    @JsonProperty("resource")
    private V1KafkaAccessResourceMatcher resource;
    @JsonProperty("allow_operations")
    @Singular
    private List<AccessOperationPolicy> allowOperations = new ArrayList<AccessOperationPolicy>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaAccessPermission() {
    }

    /**
     * 
     * @param resource
     * @param allowOperations
     */
    @ConstructorProperties({
        "resource",
        "allowOperations"
    })
    public V1KafkaAccessPermission(V1KafkaAccessResourceMatcher resource, List<AccessOperationPolicy> allowOperations) {
        super();
        this.resource = resource;
        this.allowOperations = allowOperations;
    }

    @JsonProperty("resource")
    public V1KafkaAccessResourceMatcher getResource() {
        return resource;
    }

    @JsonProperty("allow_operations")
    public List<AccessOperationPolicy> getAllowOperations() {
        return allowOperations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaAccessPermission.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("resource");
        sb.append('=');
        sb.append(((this.resource == null)?"<null>":this.resource));
        sb.append(',');
        sb.append("allowOperations");
        sb.append('=');
        sb.append(((this.allowOperations == null)?"<null>":this.allowOperations));
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
        result = ((result* 31)+((this.resource == null)? 0 :this.resource.hashCode()));
        result = ((result* 31)+((this.allowOperations == null)? 0 :this.allowOperations.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaAccessPermission) == false) {
            return false;
        }
        V1KafkaAccessPermission rhs = ((V1KafkaAccessPermission) other);
        return (((this.resource == rhs.resource)||((this.resource!= null)&&this.resource.equals(rhs.resource)))&&((this.allowOperations == rhs.allowOperations)||((this.allowOperations!= null)&&this.allowOperations.equals(rhs.allowOperations))));
    }

}
