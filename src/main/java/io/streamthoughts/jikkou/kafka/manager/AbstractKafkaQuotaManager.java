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
package io.streamthoughts.jikkou.kafka.manager;

import io.streamthoughts.jikkou.kafka.change.ChangeExecutor;
import io.streamthoughts.jikkou.kafka.change.ChangeResult;
import io.streamthoughts.jikkou.kafka.change.QuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.change.QuotaChangeOptions;
import io.streamthoughts.jikkou.kafka.model.V1SpecObject;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.model.V1QuotaObject;
import io.streamthoughts.jikkou.kafka.operation.quotas.QuotaOperation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractKafkaQuotaManager implements KafkaQuotaManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> update(final UpdateMode mode,
                                                        final List<V1SpecObject> objects,
                                                        final KafkaResourceUpdateContext<QuotaChangeOptions> context) {

        return objects.stream()
            .flatMap(spec -> {
                // Get the list of quotas, that are candidates for this execution, from the SpecsFile.
                final List<V1QuotaObject> expectedStates = spec.quotas();

                // Get the list of quotas, that are candidates for this execution, from the remote Kafka cluster
                List<V1QuotaObject> actualStates = describe(new DescribeOptions() {});

                // Compute state changes
                Supplier<List<QuotaChange>> supplier = () -> new QuotaChangeComputer().
                        computeChanges(actualStates, expectedStates, context.getOptions());

                return ChangeExecutor.ofSupplier(supplier)
                        .execute(getOperationFor(mode), context.isDryRun())
                        .stream();
            }).toList();

    }

    protected abstract QuotaOperation getOperationFor(final UpdateMode mode);

}
