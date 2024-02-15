/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.consumer;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ConsumerGroupChangeDescription implements TextDescription {

    private final ResourceChange object;

    public ConsumerGroupChangeDescription(final @NotNull ResourceChange object) {
        this.object = Objects.requireNonNull(object, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        String name = object.getMetadata().getName();
        return String.format("%s consumer group '%s'",
                object.getSpec().getOp().humanize(),
                name
        );
    }
}
