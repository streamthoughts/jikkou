/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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