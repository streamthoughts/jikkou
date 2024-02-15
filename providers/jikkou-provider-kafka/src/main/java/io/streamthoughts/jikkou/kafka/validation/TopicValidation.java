/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaTopic}.
 */
@SupportedResource(type = V1KafkaTopic.class)
public abstract class TopicValidation implements Validation<V1KafkaTopic> {

    private ExtensionContext context;

    /** {@inheritDoc} */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        this.context = context;
    }

    public ExtensionContext context() {
        return Optional.ofNullable(context).orElseThrow(() -> new IllegalStateException("not configured."));
    }
}
