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

import io.streamthoughts.jikkou.api.change.ChangeComputer;
import io.streamthoughts.jikkou.api.filter.KafkaResourceFilter;

/**
 * Represents the update context used for updating Kafka resources.
 *
 * @param <OP> type of the options for computing changes.
 *
 * @see KafkaResourceManager
 */
public interface KafkaResourceUpdateContext<OP extends ChangeComputer.Options> {

    /**
     * @return  the predicate to be used for filtering resources to update.
     */
    KafkaResourceFilter getResourceFilter();

    /**
     * @return  the options to be used for computing resource changes.
     */
    OP getOptions();

    /**
     * @return  {@code true} if the update operation should be run in dry-mode. Otherwise {@code false}.
     */
    default boolean isDryRun() {
        return true;
    }

    /**
     * Helper method to create a new {@link KafkaResourceUpdateContext} for the given arguments.
     *
     * @param options      the options for computing resource changes.
     * @param isDryRun     specify if the update should be run in dry-run.
     * @param <OP>         the type of options.
     * @return             a new {@link KafkaResourceUpdateContext}
     */
    static <OP extends ChangeComputer.Options> KafkaResourceUpdateContext<OP> with(final OP options,
                                                                                   final boolean isDryRun) {
        return with(it -> true, options, isDryRun);
    }

    /**
     * Helper method to create a new {@link KafkaResourceUpdateContext} for the given arguments.
     *
     * @param filter    the predicate for filtering resource.
     * @param options      the options for computing resource changes.
     * @param isDryRun     specify if the update should be run in dry-run.
     * @param <OP>         the type of options.
     * @return             a new {@link KafkaResourceUpdateContext}
     */
    static <OP extends ChangeComputer.Options> KafkaResourceUpdateContext<OP> with(final KafkaResourceFilter filter,
                                                                                   final OP options,
                                                                                   final boolean isDryRun) {
        return new KafkaResourceUpdateContext<>() {
            @Override
            public KafkaResourceFilter getResourceFilter() {
                return filter;
            }

            @Override
            public OP getOptions() {
                return options;
            }

            @Override
            public boolean isDryRun() {
                return isDryRun;
            }
        };
    }
}
