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

import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * A task that run a reconciliation for a resource.
 */
@InterfaceStability.Evolving
public interface ReconciliationCallable extends Callable<Collection<ChangeResult<?>>> {

    /**
     * Executes this reconciliation and returns the results for all the
     * changes applied on the resource.
     *
     * @return the collection of {@link ChangeResult}.
     */
    @Override
    Collection<ChangeResult<?>> call();

    /**
     * Gets the resource for which the reconciliation will be executed.
     * @return  the resource.
     */
    HasMetadata resource();

}
