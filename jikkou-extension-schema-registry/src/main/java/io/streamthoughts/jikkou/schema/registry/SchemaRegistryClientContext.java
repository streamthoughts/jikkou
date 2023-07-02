/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApiFactory;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A context class used to get new client for schema registry.
 */
public class SchemaRegistryClientContext {

    private final SchemaRegistryClientConfig config;

    /**
     * Creates a new {@link SchemaRegistryClientContext} instance.
     *
     * @param config  the config.
     */
    public SchemaRegistryClientContext(@NotNull Configuration config) {
        this(new SchemaRegistryClientConfig(config));
    }

    /**
     * Creates a new {@link SchemaRegistryClientContext} instance.
     *
     * @param config  the config.
     */
    public SchemaRegistryClientContext(@NotNull SchemaRegistryClientConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public SchemaRegistryApi getClientApi() {
        return SchemaRegistryApiFactory.create(config);
    }

    public AsyncSchemaRegistryApi getAsyncClientApi() {
        return new AsyncSchemaRegistryApi(getClientApi());
    }
}
