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
package io.streamthoughts.jikkou.client.command.config;

import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import io.streamthoughts.jikkou.common.utils.Strings;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@CommandLine.Command(name = "set-context", description = "Configures the specified context with the provided arguments")
public class SetContextCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Context name")
    String contextName;

    @Option(names = { "--config-props-file" }, description = "Configuration properties file for client")
    String[] clientConfigPropertiesFile;

    @Option(names = { "--config-props" }, description = "Configuration properties for client")
    String[] clientConfigProperties;

    @Option(names = { "--config-file" }, description = "Configuration file for client")
    String clientConfigFile;

    @Override
    public Integer call() throws IOException {
        ConfigurationContext context = new ConfigurationContext();

        Properties clientConfigProps = new Properties();

        if (this.clientConfigPropertiesFile != null) {
            for (final String propertiesFile : clientConfigPropertiesFile) {
                if (!Strings.isBlank(propertiesFile)) {
                    final String expandedPath = Paths.get(propertiesFile).toAbsolutePath().toString();
                    try (var reader = new FileReader(expandedPath)) {
                        clientConfigProps.load(reader);
                    }
                }
            }
        }

        if (this.clientConfigProperties != null) {
            for (final String clientConfigString : this.clientConfigProperties) {
                try {
                    clientConfigProps.putAll(Strings.toProperties(clientConfigString));
                }
                catch (Exception exception) {
                    System.out.println("The provided client configuration is not valid!");
                    return 1;
                }
            }
        }

        final var clientConfigMap = new HashMap<String, Object>();
        for (final String name : clientConfigProps.stringPropertyNames()) {
            clientConfigMap.put(name, clientConfigProps.getProperty(name));
        }

        context.setContext(contextName, new Context(clientConfigFile, clientConfigMap));
        System.out.println("Configured context " + contextName);

        if (!context.getCurrentContextName().equals(contextName)) {
            System.out.println("Run jikkou config use-context " + contextName + " for using this context");
        }

        return 0;
    }

}
