/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.context.Context;
import io.streamthoughts.jikkou.common.utils.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "set-context",
        description = "Configures the specified context with the provided arguments"
)
@Singleton
public class SetContextCommand extends CLIBaseCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Context name")
    String contextName;

    @Option(names = {"--config-props-file"}, description = "Configuration properties file for client")
    String[] clientConfigPropertiesFile;

    @Option(names = {"--config-props"}, description = "Configuration properties for client")
    String[] clientConfigProperties;

    @Option(names = {"--config-file"}, description = "Configuration file for client")
    String clientConfigFile;

    @Inject
    private ConfigurationContext configurationContext;

    @Override
    public Integer call() throws IOException {
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
                } catch (Exception exception) {
                    System.out.println("The provided client configuration is not valid!");
                    return 1;
                }
            }
        }

        final var clientConfigMap = new HashMap<String, Object>();
        for (final String name : clientConfigProps.stringPropertyNames()) {
            clientConfigMap.put(name, clientConfigProps.getProperty(name));
        }

        configurationContext.setContext(contextName, new Context(clientConfigFile, clientConfigMap));
        System.out.println("Configured context " + contextName);

        if (!configurationContext.getCurrentContextName().equals(contextName)) {
            System.out.println("Run jikkou config use-context " + contextName + " for using this context");
        }

        return 0;
    }

}
