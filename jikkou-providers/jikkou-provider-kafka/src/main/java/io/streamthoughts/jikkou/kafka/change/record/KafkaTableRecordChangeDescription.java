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

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides textual description for {@link KafkaTableRecordChange}.
 */
public class KafkaTableRecordChangeDescription implements TextDescription {

    private final ResourceChange change;

    /**
     * Creates a new {@link KafkaTableRecordChangeDescription} instance.
     *
     * @param change The change.
     */
    public KafkaTableRecordChangeDescription(final ResourceChange change) {
        this.change = Objects.requireNonNull(change, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        SpecificStateChange<V1KafkaTableRecordSpec> change = this.change.getSpec()
                .getChanges()
                .getLast("record", TypeConverter.of(V1KafkaTableRecordSpec.class));
        String keyRawValue = Optional.ofNullable(change.getAfter()).orElse(change.getBefore()).getKey().data().rawValue();
        Operation op = this.change.getSpec().getOp();
        return String.format("%s record for key '%s' into topic '%s'",
                op.humanize(),
                keyRawValue,
                op == Operation.DELETE ?
                        change.getBefore().getTopic() :
                        change.getAfter().getTopic()
        );
    }
}