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
