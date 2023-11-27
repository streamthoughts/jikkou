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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;

@JsonPropertyOrder({
        "operation",
        "record"
})
public record KafkaTableRecordChange(@JsonProperty("operation") ChangeType operation,
                                     @JsonProperty("record") ValueChange<V1KafkaTableRecordSpec> record) implements Change {


    public static KafkaTableRecordChangeBuilder builder() {
        return new KafkaTableRecordChangeBuilder();
    }

    public static class KafkaTableRecordChangeBuilder {
        private ChangeType changeType;
        private ValueChange<V1KafkaTableRecordSpec> record;

        KafkaTableRecordChangeBuilder() {
        }

        public KafkaTableRecordChangeBuilder withChangeType(ChangeType changeType) {
            this.changeType = changeType;
            return this;
        }

        public KafkaTableRecordChangeBuilder withRecord(ValueChange<V1KafkaTableRecordSpec> record) {
            this.record = record;
            return this;
        }

        public KafkaTableRecordChange build() {
            return new KafkaTableRecordChange(this.changeType, this.record);
        }
    }
}
