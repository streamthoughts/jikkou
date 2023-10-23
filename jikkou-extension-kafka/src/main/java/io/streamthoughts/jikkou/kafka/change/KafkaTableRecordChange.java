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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.jikkou.core.change.Change;
import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder(setterPrefix = "with")
@AllArgsConstructor
public final class KafkaTableRecordChange implements Change {

    private final String topic;
    private final ChangeType changeType;
    private final ValueChange<V1KafkaTableRecordSpec> record;

    public KafkaTableRecordChange(ChangeType changeType,
                                  String topic,
                                  ValueChange<V1KafkaTableRecordSpec> record) {
        this.changeType = changeType;
        this.topic = topic;
        this.record = record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeType operation() {
        return changeType;
    }

    @JsonIgnore
    public String getTopic() {
        return topic;
    }

    @JsonUnwrapped
    public ValueChange<V1KafkaTableRecordSpec> getRecord() {
        return record;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaTableRecordChange that = (KafkaTableRecordChange) o;
        return Objects.equals(topic, that.topic) &&
                changeType == that.changeType &&
                Objects.equals(record, that.record);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(topic, changeType, record);
    }
}
