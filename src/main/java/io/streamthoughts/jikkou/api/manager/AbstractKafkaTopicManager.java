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
package io.streamthoughts.jikkou.api.manager;

import io.streamthoughts.jikkou.api.change.ChangeExecutor;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.TopicChangeOptions;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.api.change.TopicChange;
import io.streamthoughts.jikkou.api.change.TopicChangeComputer;
import io.streamthoughts.jikkou.api.model.V1TopicObject;
import io.streamthoughts.jikkou.api.operation.topics.TopicOperation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractKafkaTopicManager implements KafkaTopicManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> update(final UpdateMode mode,
                                                        final List<V1SpecObject> objects,
                                                        final KafkaResourceUpdateContext<TopicChangeOptions> context) {

        return objects.stream()
            .flatMap(spec -> {
                // Get the list of topics, that are candidates for this execution, from the SpecsFile.
                final Collection<V1TopicObject> expectedStates = spec.topics()
                        .stream()
                        .filter(it -> context.getResourceFilter().test(it.name()))
                        .toList();

                // Compute state changes
                Supplier<List<TopicChange>> supplier = () -> {

                    // Get the list of topics, that are candidates for this execution, from the remote Kafka cluster
                    TopicDescribeOptions options = new TopicDescribeOptions()
                            .withDescribeDefaultConfigs(true)
                            .withTopicPredicate(context.getResourceFilter());

                    final Collection<V1TopicObject> actualStates = describe(options);

                    return new TopicChangeComputer().
                            computeChanges(actualStates, expectedStates, context.getOptions());
                };

                return ChangeExecutor
                        .ofSupplier(supplier)
                        .execute(getOperationFor(mode), context.isDryRun())
                        .stream();
            }).toList();

    }

    protected abstract TopicOperation getOperationFor(final UpdateMode mode);

}
