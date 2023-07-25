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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.List;

/**
 * This interface is used for computing all changes required
 * to reconcile a current object with its expected state.
 *
 * @param <S> the type of the object state.
 * @param <C> the type of the {@link Change} that will be computed.
 */
@InterfaceStability.Evolving
public interface ChangeComputer<S, C extends Change> {


    /**
     * Computes the changes between the given states.
     *
     * @param actualStates   the actual states.
     * @param expectedStates the expected states.
     * @return the list of state changes.
     */
    List<? extends C> computeChanges(Iterable<S> actualStates,
                                     Iterable<S> expectedStates);

}
