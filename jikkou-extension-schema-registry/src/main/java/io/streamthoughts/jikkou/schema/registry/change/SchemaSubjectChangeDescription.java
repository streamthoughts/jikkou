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
package io.streamthoughts.jikkou.schema.registry.change;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ValueChange;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class SchemaSubjectChangeDescription implements ChangeDescription {

    private final SchemaSubjectChange change;

    public SchemaSubjectChangeDescription(final @NotNull SchemaSubjectChange change) {
        this.change = Objects.requireNonNull(change, "change must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String textual() {
        return String.format("%s subject '%s' (type=%s, compatibilityLevel=%s)",
                ChangeDescription.humanize(change.getChangeType()),
                change.getSubject(),
                change.getChangeType().name(),
                Optional.ofNullable(change.getCompatibilityLevels()).map(ValueChange::getAfter)
                        .map(Enum::name)
                        .orElse("<global>")
        );
    }
}
