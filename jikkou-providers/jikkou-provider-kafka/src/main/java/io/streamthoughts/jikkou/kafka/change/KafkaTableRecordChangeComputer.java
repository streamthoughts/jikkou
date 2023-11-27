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

import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class KafkaTableRecordChangeComputer
        extends ResourceChangeComputer<V1KafkaTableRecord, V1KafkaTableRecord, KafkaTableRecordChange> {

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public KafkaTableRecordChangeComputer() {
        super(new V1KafkaTableRecordChangeKeyMapper(), identityChangeValueMapper(), false);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<KafkaTableRecordChange> buildChangeForCreating(@NotNull V1KafkaTableRecord after) {
        return List.of(buildChange(ChangeType.ADD, ValueChange.withAfterValue(after.getSpec())));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<KafkaTableRecordChange> buildChangeForDeleting(@NotNull V1KafkaTableRecord before) {
        return List.of(buildChange(ChangeType.DELETE, ValueChange.withBeforeValue(before.getSpec())));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<KafkaTableRecordChange> buildChangeForNone(@NotNull V1KafkaTableRecord before, @NotNull V1KafkaTableRecord after) {
        return buildChangeForUpdating(before, after);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<KafkaTableRecordChange> buildChangeForUpdating(@NotNull V1KafkaTableRecord before,
                                                               @NotNull V1KafkaTableRecord after) {
        ValueChange<V1KafkaTableRecordSpec> record = ValueChange.with(
                before.getSpec(),
                after.getSpec()
        );
        return List.of(buildChange(record.operation(), record));
    }


    private static KafkaTableRecordChange buildChange(ChangeType type,
                                                      ValueChange<V1KafkaTableRecordSpec> record) {
        return new KafkaTableRecordChange(type,  record);
    }

    /**
     * Topic Name and a Record Key.
     *
     * @param topic the topic name.
     * @param key   the record key.
     */
    record TopicAndKey(@NotNull String topic, @NotNull DataValue key) {

    }

    private static class V1KafkaTableRecordChangeKeyMapper implements ChangeKeyMapper<V1KafkaTableRecord> {
        @Override
        public @NotNull Object apply(@NotNull V1KafkaTableRecord object) {
            return new TopicAndKey(object.getSpec().getTopic(), object.getSpec().getKey());
        }
    }
}
