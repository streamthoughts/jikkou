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

import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.DELETE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.IGNORE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.NONE;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import java.util.List;

public final class ConsumerGroupChangeComputer extends ResourceChangeComputer<
        V1KafkaConsumerGroup,
        V1KafkaConsumerGroup,
        ValueChange<V1KafkaConsumerGroup>> {
    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public ConsumerGroupChangeComputer() {
        super(metadataNameKeyMapper(), identityChangeValueMapper(), false);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    protected ChangeType getChangeType(V1KafkaConsumerGroup before, V1KafkaConsumerGroup after) {
       return (before == null || after == null) ? IGNORE : CoreAnnotations.isAnnotatedWithDelete(after) ? DELETE : NONE;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ValueChange<V1KafkaConsumerGroup>> buildChangeForDeleting(V1KafkaConsumerGroup before) {
        return List.of(ValueChange.withBeforeValue(before));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ValueChange<V1KafkaConsumerGroup>> buildChangeForUpdating(V1KafkaConsumerGroup before,
                                                                          V1KafkaConsumerGroup after) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ValueChange<V1KafkaConsumerGroup>> buildChangeForNone(V1KafkaConsumerGroup before,
                                                                      V1KafkaConsumerGroup after) {
        // NONE
        return List.of(ValueChange.none(before, after));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ValueChange<V1KafkaConsumerGroup>> buildChangeForCreating(V1KafkaConsumerGroup after) {
        throw new UnsupportedOperationException();
    }
}
