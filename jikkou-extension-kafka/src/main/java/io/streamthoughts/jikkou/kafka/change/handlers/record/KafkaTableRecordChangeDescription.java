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
package io.streamthoughts.jikkou.kafka.change.handlers.record;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Provides textual description for {@link KafkaTableRecordChange}.
 */
public class KafkaTableRecordChangeDescription implements ChangeDescription {

    private final HasMetadataChange<KafkaTableRecordChange> item;

    /**
     * Creates a new {@link KafkaTableRecordChangeDescription} instance.
     *
     * @param item the item change.
     */
    public KafkaTableRecordChangeDescription(final @NotNull HasMetadataChange<KafkaTableRecordChange> item) {
        this.item = Objects.requireNonNull(item, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        KafkaTableRecordChange change = item.getChange();
        ValueChange<V1KafkaTableRecordSpec> record = change.getRecord();
        String keyRawValue = Optional.ofNullable(record.getAfter()).orElse(record.getBefore()).getKey().data().rawValue();
        return String.format("%s record for key '%s' into topic '%s'",
                ChangeDescription.humanize(change.operation()),
                keyRawValue,
                change.getTopic()
        );
    }
}
