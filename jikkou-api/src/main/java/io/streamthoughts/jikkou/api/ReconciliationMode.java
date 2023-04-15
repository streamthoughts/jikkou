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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeType;
import java.util.Set;

/**
 * Determines the type of changes that can be applied on the resources to be reconciled.
 */
public enum ReconciliationMode {

    /** Only apply changes creating new resource items */
    CREATE(ChangeType.ADD),

    /** Only apply changes deleting orphan resource items */
    DELETE(ChangeType.DELETE),

    /** Only apply changes altering existing resource items */
    UPDATE(ChangeType.ADD, ChangeType.UPDATE),

    /** Apply all reconciliation changes */
    APPLY(ChangeType.ADD, ChangeType.UPDATE, ChangeType.DELETE);

    /**
     * Set of change-type supported for this reconciliation mode.
     */
    private final Set<ChangeType> changeTypes;

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param changeTypes   {@link #changeTypes}
     */
    ReconciliationMode(ChangeType... changeTypes) {
        this(Set.of(changeTypes));
    }

    /**
     * Creates a new {@link ReconciliationMode} instance.
     *
     * @param changeTypes   {@link #changeTypes}
     */
    ReconciliationMode(Set<ChangeType> changeTypes) {
        this.changeTypes = changeTypes;
    }

    /**
     * Checks whether the given change is supported by this reconciliation mode.
     *
     * @param change    the change to test.
     * @return  {@code true} if the change is supported, otherwise {@code false}.
     */
    public boolean isSupported(Change change) {
        ChangeType changeType = change.getChangeType();
        return changeType == ChangeType.NONE || changeTypes.contains(changeType);
    }
}
