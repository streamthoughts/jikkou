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
package io.streamthoughts.jikkou.kafka.change.record;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import org.jetbrains.annotations.NotNull;

public final class KafkaTableRecordChangeComputer
        extends ResourceChangeComputer<Object, V1KafkaTableRecord, ResourceChange> {

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public KafkaTableRecordChangeComputer() {
        super(
                object -> new TopicAndKey(object.getSpec().getTopic(), object.getSpec().getKey()),
                new KafkaTableRecordChangeFactory()
        );
    }

    public static class KafkaTableRecordChangeFactory extends ResourceChangeFactory<Object, V1KafkaTableRecord, ResourceChange> {

        public static final String DATA_RECORD = "record";

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForCreate(Object key, V1KafkaTableRecord after) {
            return GenericResourceChange
                    .builder(V1KafkaTableRecord.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Operation.CREATE)
                            .withChange(StateChange.create(DATA_RECORD, after.getSpec()))
                            .build()
                    )
                    .build();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForDelete(Object key, V1KafkaTableRecord before) {
            return GenericResourceChange
                    .builder(V1KafkaTableRecord.class)
                    .withMetadata(before.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Operation.DELETE)
                            .withChange(StateChange.delete(DATA_RECORD, before.getSpec()))
                            .build()
                    )
                    .build();
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public ResourceChange createChangeForUpdate(Object key, V1KafkaTableRecord before, V1KafkaTableRecord after) {
            SpecificStateChange<V1KafkaTableRecordSpec> change = StateChange.with(
                    DATA_RECORD,
                    before.getSpec(),
                    after.getSpec()
            );
            return GenericResourceChange
                    .builder(V1KafkaTableRecord.class)
                    .withMetadata(before.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(change.getOp())
                            .withChange(change)
                            .build()
                    )
                    .build();
        }
    }

    /**
     * Topic Name and a Record Key.
     *
     * @param topic the topic name.
     * @param key   the record key.
     */
    record TopicAndKey(@NotNull String topic, @NotNull DataValue key) {

    }
}
