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
