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

import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.ChangeComputer;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.model.V1BrokerObject;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;

import java.util.Collection;
import java.util.List;

/**
 * Base interface for managing Kafka Topics.
 */
public interface KafkaBrokerManager extends
        KafkaResourceManager<V1BrokerObject, Change<?>, ChangeComputer.Options, BrokerDescribeOptions> {

    /**
     * {@inheritDoc}
     */
    @Override
    default Collection<ChangeResult<Change<?>>> update(UpdateMode mode,
                                                       List<V1SpecsObject> objects,
                                                       KafkaResourceUpdateContext<ChangeComputer.Options> context) {
        throw new UnsupportedOperationException();
    }

}
