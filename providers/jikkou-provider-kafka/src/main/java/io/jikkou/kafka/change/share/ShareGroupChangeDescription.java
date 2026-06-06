/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.share;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.TextDescription;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ShareGroupChangeDescription implements TextDescription {

    private final ResourceChange object;

    public ShareGroupChangeDescription(final @NotNull ResourceChange object) {
        this.object = Objects.requireNonNull(object, "change must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String textual() {
        return String.format("%s share group '%s'",
            object.getSpec().getOp().humanize(),
            object.getMetadata().getName());
    }
}
