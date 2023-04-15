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
package io.streamthoughts.jikkou.client;

import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.JikkouContext;
import io.streamthoughts.jikkou.api.ResourceContext;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.client.configure.ResourceValidationApiConfigurator;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientContext {
    private static final Logger LOG = LoggerFactory.getLogger(ClientContext.class);

    private static final ClientContext CONTEXT = new ClientContext();

    private final JikkouContext context;

    private final JikkouConfig configuration;

    public static ClientContext get() {
        return CONTEXT;
    }

    /**
     * Creates a new {@link ClientContext} instance.
     */
    private ClientContext() {
        ConfigurationContext configurationContext = new ConfigurationContext();
        if (!configurationContext.isExists()) {
            System.err.println(
                "No configuration context has been defined." +
                " Run 'jikkou config set-context <context_name> --config=kafka.client.bootstrap.servers=localhost:9092" +
                " [--client-config=<config_string>] [--config-file=<config_file>].' to create a context."
            );
        } else {
            LOG.info("Using config context '{}' with file={}",
                    configurationContext.getCurrentContextName(),
                    configurationContext.getCurrentContext().configFile()
            );

        }
        configuration = configurationContext.getCurrentContext().load();
        context = new JikkouContext(configuration);
    }

    /**
     * Gets the current {@link JikkouConfig}.
     *
     * @return  the configuration.
     */
    public JikkouConfig getConfiguration() {
        return configuration;
    }

    /**
     * Gets the current {@link ExtensionFactory}.
     *
     * @return  the extension factory.
     */
    public ExtensionFactory getExtensionFactory() {
        return context.getExtensionFactory();
    }

    /**
     * Create a new {@link JikkouApi}.
     *
     * @return  the api.
     */
    public JikkouApi createApi() {
        return context.createApi(new ResourceValidationApiConfigurator(context.getExtensionFactory()));
    }

    /**
     * Gets the current {@link ResourceContext}.
     *
     * @return  the resource context.
     */
    public ResourceContext getResourceContext() {
        return context.getResourceContext();
    }
}
