/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.change;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;

/**
 * KafkaConnectorChangeDescription.
 */
public final class KafkaConnectorChangeDescription implements TextDescription {

    private final ResourceChange change;
    private final String cluster;

    /**
     * Creates a new {@link KafkaConnectorChangeDescription} instance.
     *
     * @param cluster the name of the kafka connect cluster.
     * @param change  the data change.
     */
    public KafkaConnectorChangeDescription(final String cluster,
                                           final ResourceChange change) {
        this.change = change;
        this.cluster = cluster;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        return String.format("%s connector '%s' on Kafka Connect cluster '%s'",
                change.getSpec().getOp().humanize(),
                change.getMetadata().getName(),
                cluster
        );
    }
}
