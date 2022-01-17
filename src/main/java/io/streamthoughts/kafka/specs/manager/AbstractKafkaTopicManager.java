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
package io.streamthoughts.kafka.specs.manager;

import io.streamthoughts.kafka.specs.change.ChangeExecutor;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChangeComputer;
import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractKafkaTopicManager implements KafkaTopicManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> update(final UpdateMode mode,
                                                        final List<V1SpecsObject> objects,
                                                        final KafkaResourceUpdateContext<TopicChangeOptions> context) {

        return objects.stream()
            .flatMap(spec -> {
                // Get the list of topics, that are candidates for this execution, from the SpecsFile.
                final Collection<V1TopicObject> expectedStates = spec.topics()
                        .stream()
                        .filter(it -> context.getPredicate().test(it.name()))
                        .toList();

                // Compute state changes
                Supplier<List<TopicChange>> supplier = () -> {

                    // Get the list of topics, that are candidates for this execution, from the remote Kafka cluster
                    TopicDescribeOptions options = new TopicDescribeOptions()
                            .withDescribeDefaultConfigs(true)
                            .withTopicPredicate(context.getPredicate());

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
