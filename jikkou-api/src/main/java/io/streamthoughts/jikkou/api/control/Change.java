/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.api.control;

import static io.streamthoughts.jikkou.api.control.ChangeType.NONE;
import static io.streamthoughts.jikkou.api.control.ChangeType.UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.Arrays;

/**
 * Represents a change operation on a resource entity.
 */
@Evolving
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface Change {

    /**
     * Gets the type of the change.
     *
     * @return the change type.
     */
    @JsonProperty("operation")
    ChangeType getChangeType();

    /**
     * Computes a common change type from all changes. This method will return {@link ChangeType#NONE} if all
     * given changes are of type {@link ChangeType#NONE}, otherwise it returns {@link ChangeType#UPDATE}.
     *
     * @param changes   the list of changes.
     * @return  a {@link ChangeType}.
     */
    static ChangeType computeChangeTypeFrom(Change... changes) {
        return Arrays.stream(changes)
                .map(Change::getChangeType)
                .reduce(NONE, (t1, t2) -> t1 == NONE && t2 == NONE ? NONE : UPDATE);
    }
}
