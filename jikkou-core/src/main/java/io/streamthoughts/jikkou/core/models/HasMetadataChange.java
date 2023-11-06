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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.models.generics.GenericResourceChange;
import io.streamthoughts.jikkou.core.reconcilier.Change;

/**
 * Resource that represents a change.
 */
@Evolving
@JsonDeserialize(as = GenericResourceChange.class)
public interface HasMetadataChange<T extends Change> extends HasMetadata {

    /**
     * Gets the change.
     *
     * @return  The change.
     */
    T getChange();
}
