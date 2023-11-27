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
package io.streamthoughts.jikkou.kafka.change.handlers.consumer;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ConsumerGroupChangeDescription implements ChangeDescription {

    private final HasMetadataChange<ValueChange<V1KafkaConsumerGroup>> object;

    public ConsumerGroupChangeDescription(final @NotNull HasMetadataChange<ValueChange<V1KafkaConsumerGroup>> object) {
        this.object = Objects.requireNonNull(object, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        ValueChange<V1KafkaConsumerGroup> change = object.getChange();
        return String.format("%s consumer group '%s'",
                ChangeDescription.humanize(change.operation()),
                change.getBefore() != null ?
                        change.getBefore().getMetadata().getName() :
                        change.getAfter().getMetadata().getName()
        );
    }
}
