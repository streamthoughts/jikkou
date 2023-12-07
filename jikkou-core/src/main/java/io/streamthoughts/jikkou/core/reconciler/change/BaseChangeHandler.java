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
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import java.util.Set;

/**
 * @param <T> Type of the {@link ResourceChange}.
 */
public abstract class BaseChangeHandler<T extends ResourceChange> implements ChangeHandler<T> {

    private final Set<Operation> supportedOperations;


    /**
     * Creates a new {@link BaseChangeHandler} instance.
     *
     * @param supportedOperation The supported change type.
     */
    public BaseChangeHandler(Operation supportedOperation) {
        this(Set.of(supportedOperation));
    }

    /**
     * Creates a new {@link BaseChangeHandler} instance.
     *
     * @param supportedOperations The supported change types.
     */
    public BaseChangeHandler(Set<Operation> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return supportedOperations;
    }
}
