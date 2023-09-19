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

import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.kafka.models.KafkaRecordData;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import java.util.List;

public class RecordChangeComputer extends ResourceChangeComputer<V1KafkaTableRecord, V1KafkaTableRecord, RecordChange> {

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public RecordChangeComputer() {
        super(
                metadataNameKeyMapper(),
                identityChangeValueMapper(),
                false
        );
    }

    /** {@inheritDoc} **/
    @Override
    public List<RecordChange> buildChangeForCreating(V1KafkaTableRecord after) {
        return List.of(buildChange(ChangeType.ADD, after, ValueChange.withAfterValue(after.getSpec().getRecord())));
    }

    /** {@inheritDoc} **/
    @Override
    public List<RecordChange> buildChangeForDeleting(V1KafkaTableRecord before) {
        return List.of(buildChange(ChangeType.DELETE, before, ValueChange.withBeforeValue(before.getSpec().getRecord())));
    }

    /** {@inheritDoc} **/
    @Override
    public List<RecordChange> buildChangeForNone(V1KafkaTableRecord before, V1KafkaTableRecord after) {
        return buildChangeForUpdating(before, after);
    }

    /** {@inheritDoc} **/
    @Override
    public List<RecordChange> buildChangeForUpdating(V1KafkaTableRecord before,
                                                     V1KafkaTableRecord after) {
        ValueChange<KafkaRecordData> record = ValueChange.with(
                before.getSpec().getRecord(),
                after.getSpec().getRecord()
        );
        RecordChange change = buildChange(record.getChangeType(), after, record);
        return List.of(change);
    }

    private static RecordChange buildChange(ChangeType type,
                                            V1KafkaTableRecord original,
                                            ValueChange<KafkaRecordData> record) {
        return RecordChange
                .builder()
                .withChangeType(type)
                .withTopic(original.getMetadata().getName())
                .withKeyFormat(original.getSpec().getKeyFormat())
                .withValueFormat(original.getSpec().getValueFormat())
                .withRecord(record)
                .build();
    }
}
