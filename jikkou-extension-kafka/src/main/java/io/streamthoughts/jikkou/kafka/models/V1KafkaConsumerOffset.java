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
package io.streamthoughts.jikkou.kafka.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;


/**
 * V1KafkaConsumerOffset
 * <p>
 * Information about the position of a consumer for a topic-partitions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Information about the position of a consumer for a topic-partitions")
@JsonPropertyOrder({
    "topic",
    "partition",
    "offset"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaConsumerOffset {

    /**
     * The Topic.
     * 
     */
    @JsonProperty("topic")
    @JsonPropertyDescription("The Topic.")
    private String topic;
    /**
     * The partition.
     * 
     */
    @JsonProperty("partition")
    @JsonPropertyDescription("The partition.")
    private Integer partition;
    /**
     * The offset.
     * 
     */
    @JsonProperty("offset")
    @JsonPropertyDescription("The offset.")
    private Object offset;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaConsumerOffset() {
    }

    /**
     * 
     * @param partition
     * @param offset
     * @param topic
     */
    @ConstructorProperties({
        "topic",
        "partition",
        "offset"
    })
    public V1KafkaConsumerOffset(String topic, Integer partition, Object offset) {
        super();
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
    }

    /**
     * The Topic.
     * 
     */
    @JsonProperty("topic")
    public String getTopic() {
        return topic;
    }

    /**
     * The partition.
     * 
     */
    @JsonProperty("partition")
    public Integer getPartition() {
        return partition;
    }

    /**
     * The offset.
     * 
     */
    @JsonProperty("offset")
    public Object getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaConsumerOffset.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("topic");
        sb.append('=');
        sb.append(((this.topic == null)?"<null>":this.topic));
        sb.append(',');
        sb.append("partition");
        sb.append('=');
        sb.append(((this.partition == null)?"<null>":this.partition));
        sb.append(',');
        sb.append("offset");
        sb.append('=');
        sb.append(((this.offset == null)?"<null>":this.offset));
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
        result = ((result* 31)+((this.offset == null)? 0 :this.offset.hashCode()));
        result = ((result* 31)+((this.topic == null)? 0 :this.topic.hashCode()));
        result = ((result* 31)+((this.partition == null)? 0 :this.partition.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaConsumerOffset) == false) {
            return false;
        }
        V1KafkaConsumerOffset rhs = ((V1KafkaConsumerOffset) other);
        return ((((this.offset == rhs.offset)||((this.offset!= null)&&this.offset.equals(rhs.offset)))&&((this.topic == rhs.topic)||((this.topic!= null)&&this.topic.equals(rhs.topic))))&&((this.partition == rhs.partition)||((this.partition!= null)&&this.partition.equals(rhs.partition))));
    }

}
