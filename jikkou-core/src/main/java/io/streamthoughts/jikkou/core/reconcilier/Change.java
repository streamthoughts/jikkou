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
package io.streamthoughts.jikkou.core.reconcilier;

import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.NONE;
import static io.streamthoughts.jikkou.core.reconcilier.ChangeType.UPDATE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.generics.GenericChange;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a change operation on a resource entity.
 */
@Evolving
@Reflectable
@JsonDeserialize(as = GenericChange.class)
public interface Change {

    /**
     * Gets the type of operation that changed the data.
     *
     * @return a change type.
     */
    ChangeType operation();

    /**
     * Computes a common change type from all changes. This method will return {@link ChangeType#NONE} if all
     * given changes are of type {@link ChangeType#NONE}, otherwise it returns {@link ChangeType#UPDATE}.
     *
     * @param changes   the list of changes.
     * @return  a {@link ChangeType}.
     */
    static ChangeType computeChangeTypeFrom(Change... changes) {
        return computeChangeTypeFrom(Arrays.asList(changes));
    }

    /**
     * Computes a common change type from all changes. This method will return {@link ChangeType#NONE} if all
     * given changes are of type {@link ChangeType#NONE}, otherwise it returns {@link ChangeType#UPDATE}.
     *
     * @param changes   the list of changes.
     * @return  a {@link ChangeType}.
     */
    static ChangeType computeChangeTypeFrom(List<Change> changes) {
        return changes
                .stream()
                .map(Change::operation)
                .reduce(NONE, (t1, t2) -> t1 == NONE && t2 == NONE ? NONE : UPDATE);
    }
}
