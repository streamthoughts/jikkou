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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import lombok.Builder;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Setter
@JsonPropertyOrder({
    "keyFormat",
    "valueFormat",
    "record"
})
@Jacksonized
@Reflectable
@Generated("jsonschema2pojo")
public class V1KafkaTableRecordSpec {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyFormat")
    private io.streamthoughts.jikkou.kafka.model.DataFormat keyFormat;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("valueFormat")
    private io.streamthoughts.jikkou.kafka.model.DataFormat valueFormat;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("record")
    private KafkaRecordData record;

    /**
     * No args constructor for use in serialization
     * 
     */
    public V1KafkaTableRecordSpec() {
    }

    /**
     * 
     * @param keyFormat
     * @param record
     * @param valueFormat
     */
    @ConstructorProperties({
        "keyFormat",
        "valueFormat",
        "record"
    })
    public V1KafkaTableRecordSpec(io.streamthoughts.jikkou.kafka.model.DataFormat keyFormat, io.streamthoughts.jikkou.kafka.model.DataFormat valueFormat, KafkaRecordData record) {
        super();
        this.keyFormat = keyFormat;
        this.valueFormat = valueFormat;
        this.record = record;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyFormat")
    public io.streamthoughts.jikkou.kafka.model.DataFormat getKeyFormat() {
        return keyFormat;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("valueFormat")
    public io.streamthoughts.jikkou.kafka.model.DataFormat getValueFormat() {
        return valueFormat;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("record")
    public KafkaRecordData getRecord() {
        return record;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(V1KafkaTableRecordSpec.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("keyFormat");
        sb.append('=');
        sb.append(((this.keyFormat == null)?"<null>":this.keyFormat));
        sb.append(',');
        sb.append("valueFormat");
        sb.append('=');
        sb.append(((this.valueFormat == null)?"<null>":this.valueFormat));
        sb.append(',');
        sb.append("record");
        sb.append('=');
        sb.append(((this.record == null)?"<null>":this.record));
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
        result = ((result* 31)+((this.valueFormat == null)? 0 :this.valueFormat.hashCode()));
        result = ((result* 31)+((this.keyFormat == null)? 0 :this.keyFormat.hashCode()));
        result = ((result* 31)+((this.record == null)? 0 :this.record.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof V1KafkaTableRecordSpec) == false) {
            return false;
        }
        V1KafkaTableRecordSpec rhs = ((V1KafkaTableRecordSpec) other);
        return ((((this.valueFormat == rhs.valueFormat)||((this.valueFormat!= null)&&this.valueFormat.equals(rhs.valueFormat)))&&((this.keyFormat == rhs.keyFormat)||((this.keyFormat!= null)&&this.keyFormat.equals(rhs.keyFormat))))&&((this.record == rhs.record)||((this.record!= null)&&this.record.equals(rhs.record))));
    }

}
