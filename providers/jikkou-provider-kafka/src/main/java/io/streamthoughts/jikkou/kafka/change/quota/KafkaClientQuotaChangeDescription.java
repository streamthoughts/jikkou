/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.quota;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import java.util.Objects;
import java.util.stream.Collectors;

public final class KafkaClientQuotaChangeDescription implements TextDescription {

    private final ResourceChange change;

    public KafkaClientQuotaChangeDescription(final ResourceChange change) {
        this.change = Objects.requireNonNull(change, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        return String.format("%s quotas for entity=%s, constraints=[%s])",
                change.getSpec().getOp().humanize(),
                change.getSpec().getData(),
                change.getSpec().getChanges()
                        .stream()
                        .map(it -> it.getName() + "=" + it.getAfter())
                        .collect(Collectors.joining(","))
        );
    }
}