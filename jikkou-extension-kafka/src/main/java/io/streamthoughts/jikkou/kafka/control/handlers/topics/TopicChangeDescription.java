/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.control.handlers.topics;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class TopicChangeDescription implements ChangeDescription {

    private final TopicChange change;

    public TopicChangeDescription(final @NotNull TopicChange change) {
        this.change = Objects.requireNonNull(change, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ChangeType type() {
        return change.getChangeType();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        return String.format("%s topic '%s' (partitions=%d, replicas=%d, configs=[%s])",
                ChangeDescription.humanize(type()),
                change.getName(),
                Optional.ofNullable(change.getPartitions()).map(ValueChange::getAfter).orElse(null),
                Optional.ofNullable(change.getReplicas()).map(ValueChange::getAfter).orElse(null),
                change.getConfigEntryChanges()
                        .stream()
                        .map(s -> s.getName() + "=" + s.getValueChange().getAfter())
                        .collect(Collectors.joining(","))
        );
    }
}
