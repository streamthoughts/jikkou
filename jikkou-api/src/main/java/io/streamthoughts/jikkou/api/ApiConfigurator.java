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

import io.streamthoughts.jikkou.api.config.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Interface that can be used to configure {@link JikkouApi} using a given
 * {@link JikkouApi.ApiBuilder}, and a {@link Configuration}.
 */
public interface ApiConfigurator {

    <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(
            B builder,
            Configuration configuration
    );

    static ApiConfigurator.List emptyList() {
        return new List();
    }

    static ApiConfigurator.List listOf(final ApiConfigurator configurator) {
        return new List(java.util.List.of(configurator));
    }

    static ApiConfigurator.List listOf(final java.util.List<ApiConfigurator> configurators) {
        return new List(configurators);
    }

    /**
     * An immutable list of {@link ApiConfigurator}
     */
    class List implements ApiConfigurator {

        private final java.util.List<ApiConfigurator> apiConfigurators;

        /**
         * Creates a new {@link ApiConfigurator.List} instance.
         */
        private List() {
            this(Collections.emptyList());
        }

        /**
         * Creates a new {@link ApiConfigurator.List} instance.
         */
        private List(final java.util.List<ApiConfigurator> apiConfigurators) {
            this.apiConfigurators = new ArrayList<>(apiConfigurators);
        }

        public ApiConfigurator.List with(final ApiConfigurator... configurators) {
            if (configurators.length == 0) {
                return this;
            }
            LinkedList<ApiConfigurator> all = new LinkedList<>(apiConfigurators);
            for (ApiConfigurator f : configurators) {
                if (f != null) {
                    all.add(f);
                }
            }
            return new List(all);
        }

        public ApiConfigurator.List with(final Iterable<ApiConfigurator> configurators) {
            LinkedList<ApiConfigurator> all = new LinkedList<>(apiConfigurators);
            configurators.forEach(field -> {
                if (field != null) {
                    all.add(field);
                }
            });
            return new List(all);
        }

        public ApiConfigurator.List with(final ApiConfigurator configurator) {
            LinkedList<ApiConfigurator> all = new LinkedList<>(apiConfigurators);
            all.add(configurator);
            return new List(all);
        }

        /** {@inheritDoc} **/
        @Override
        public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(final B builder,
                                                                                       final Configuration configuration) {
            B result = builder;
            for (ApiConfigurator configurator : apiConfigurators) {
                result = configurator.configure(builder, configuration);
            }
            return result;
        }
    }
}
