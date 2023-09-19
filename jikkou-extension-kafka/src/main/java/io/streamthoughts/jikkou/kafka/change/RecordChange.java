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
package io.streamthoughts.jikkou.kafka.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.kafka.model.DataFormat;
import io.streamthoughts.jikkou.kafka.models.KafkaRecordData;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@AllArgsConstructor
@JsonPropertyOrder({"topic", "keyFormat", "valueFormat", "record"})
@Getter
public final class RecordChange implements Change {

    private final String topic;
    private final ChangeType changeType;
    private final DataFormat keyFormat;
    private final DataFormat valueFormat;
    private final ValueChange<KafkaRecordData> record;

    public RecordChange(ChangeType changeType,
                        String topic,
                        DataFormat keyFormat,
                        DataFormat valueFormat,
                        ValueChange<KafkaRecordData> record) {
        this.changeType = changeType;
        this.topic = topic;
        this.keyFormat = keyFormat;
        this.valueFormat = valueFormat;
        this.record = record;
    }

    /**
     * {@inheritDoc}
     */
    @JsonProperty("operation")
    @Override
    public ChangeType getChangeType() {
        return changeType;
    }

    @JsonIgnore
    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RecordChange) obj;
        return Objects.equals(this.topic, that.topic) &&
                Objects.equals(this.keyFormat, that.keyFormat) &&
                Objects.equals(this.valueFormat, that.valueFormat) &&
                Objects.equals(this.record, that.record);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, keyFormat, valueFormat, record);
    }

    @Override
    public String toString() {
        return "RecordChange[" +
                "topic=" + topic + ", " +
                "keyFormat=" + keyFormat + ", " +
                "valueFormat=" + valueFormat + ", " +
                "record=" + record + ']';
    }

}
