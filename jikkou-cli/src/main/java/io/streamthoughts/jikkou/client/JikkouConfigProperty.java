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
package io.streamthoughts.jikkou.client;

import com.typesafe.config.Config;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

/**
 * The list of configuration parameters.
 */
public final class JikkouConfigProperty {

    public static final ConfigProperty<List<String>> EXTENSION_PATHS = ConfigProperty
            .ofList("extension.paths");

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of {@link Config}.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<List<? extends Config>> ofConfigs(final @NotNull String path) {
        BiFunction<String, Configuration, Optional<List<? extends Config>>> supplier = (p, config) ->
                Optional.of(config.hasKey(p) ?
                        ((JikkouConfig)config).unwrap().getConfigList(p) :
                        Collections.emptyList()
                );

        return new ConfigProperty<>(path, supplier);
    }
}
