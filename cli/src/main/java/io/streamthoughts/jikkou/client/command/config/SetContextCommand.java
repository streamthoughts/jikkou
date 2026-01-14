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
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "set-context",
        description = "Configures the specified context with the provided arguments"
)
@Singleton
public class SetContextCommand extends CLIBaseCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The name of the context to which the configuration applies.")
    String contextName;

    @Option(names = {"--config-props-file"}, description = "Path(s) to one or more configuration properties files.")
    String[] clientConfigPropertiesFile;

    @Option(names = {"--config-props"}, description = "Inline configuration properties in the form of key=value pairs.")
    String[] clientConfigProperties;

    @Option(names = {"--config-file"}, description = "Path to a Jikkou configuration file.")
    String clientConfigFile;

    @Option(names = {"--provider"}, description = "Name of the provider to which this configuration should be attached.")
    String provider;

    @Option(names = {"--config-prefix"}, description = "Prefix to apply to all configuration property keys.")
    String configPrefix;

    @Inject
    private ConfigurationContext configurationContext;

    @Override
    public Integer call() throws IOException {
        Properties clientConfigProps = new Properties();

        if (this.clientConfigPropertiesFile != null) {
            for (final String propertiesFile : clientConfigPropertiesFile) {
                if (!Strings.isNullOrEmpty(propertiesFile)) {
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

        Map<String, Object> clientConfigMap = new HashMap<>();
        for (final String name : clientConfigProps.stringPropertyNames()) {
            String key = configPrefix != null ? configPrefix + "." + name : name;
            clientConfigMap.put(key, clientConfigProps.getProperty(name));
        }

        if (provider != null) {
            String providerConfigKey = JikkouConfigProperties.PROVIDER_CONFIG.key() + "." + provider + ".config.";
            clientConfigMap = clientConfigMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> providerConfigKey + entry.getKey(), Map.Entry::getValue));
        }

        configurationContext.setContext(contextName, new Context(clientConfigFile, clientConfigMap));
        System.out.println("Configured context " + contextName);

        if (!configurationContext.getCurrentContextName().equals(contextName)) {
            System.out.println("Run jikkou config use-context " + contextName + " for using this context");
        }

        return 0;
    }

}
