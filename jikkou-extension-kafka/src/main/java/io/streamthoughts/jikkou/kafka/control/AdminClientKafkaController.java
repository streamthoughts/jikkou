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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

public abstract class AdminClientKafkaController implements Configurable, AutoCloseable {

    protected AdminClientContext adminClientContext;

    /**
     * Creates a new {@link AdminClientKafkaController} instance.
     */
    public AdminClientKafkaController() { }

    /**
     * Creates a new {@link AdminClientKafkaController} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaController(final @NotNull Configuration config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaController(final @NotNull AdminClientContext adminClientContext) {
        this.adminClientContext = adminClientContext;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        if (adminClientContext == null) {
            adminClientContext = new AdminClientContext(config);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public void close() {
        adminClientContext.close();
    }
}
