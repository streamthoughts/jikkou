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

import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.function.Predicate;

/**
 * Default interface for filtering resources.
 */
@InterfaceStability.Evolving
public interface ResourceFilter {

    static final ResourceFilter DEFAULT = new ResourceFilter(){};

    default Predicate<String> getPredicateByName() {
        return name -> Boolean.TRUE;
    }

    default boolean apply(final String name) {
        return getPredicateByName().test(name);
    }

    default boolean apply(final Nameable nameable) {
        return getPredicateByName().test(nameable.getName());
    }

    static ResourceFilter filterByName(Predicate<String> predicate) {
        return new ResourceFilter() {
            @Override
            public Predicate<String> getPredicateByName() {
                return predicate;
            }
        };
    }

}
