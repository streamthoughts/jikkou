/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "partitions"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaTopicStatus {

    @JsonProperty("partitions")
    @Singular
    private List<KafkaTopicPartitionInfo> partitions = new ArrayList<KafkaTopicPartitionInfo>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTopicStatus() {
    }

    /**
     * 
     * @param partitions
     */
    @ConstructorProperties({
        "partitions"
    })
    public V1KafkaTopicStatus(List<KafkaTopicPartitionInfo> partitions) {
        super();
        this.partitions = partitions;
    }

    @JsonProperty("partitions")
    public List<KafkaTopicPartitionInfo> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTopicStatus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("partitions");
        sb.append('=');
        sb.append(((this.partitions == null)?"<null>":this.partitions));
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
        result = ((result* 31)+((this.partitions == null)? 0 :this.partitions.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaTopicStatus) == false) {
            return false;
        }
        V1KafkaTopicStatus rhs = ((V1KafkaTopicStatus) other);
        return ((this.partitions == rhs.partitions)||((this.partitions!= null)&&this.partitions.equals(rhs.partitions)));
    }

}
