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
package io.streamthoughts.kafka.specs;

import io.streamthoughts.kafka.specs.command.CLIOptionsMixin;
import io.streamthoughts.kafka.specs.command.acls.AclsCommand;
import io.streamthoughts.kafka.specs.command.broker.BrokerCommand;
import io.streamthoughts.kafka.specs.command.config.ConfigCommand;
import io.streamthoughts.kafka.specs.command.extensions.ExtensionCommand;
import io.streamthoughts.kafka.specs.command.quotas.QuotasCommand;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.command.validate.ValidateCommand;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.internal.PropertiesUtils;
import org.apache.kafka.clients.admin.AdminClientConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static picocli.CommandLine.Model.CommandSpec;

/**
 * The main-class
 */
@Command(name = "Jikkou",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        headerHeading = "Usage: ",
        synopsisHeading = "%n",
        description = "CLI to ease and automate Apache Kafka cluster configuration management.",
        mixinStandardHelpOptions = true,
        versionProvider = Jikkou.ManifestVersionProvider.class,
        subcommands = {
            ValidateCommand.class,
            TopicsCommand.class,
            AclsCommand.class,
            BrokerCommand.class,
            QuotasCommand.class,
            ExtensionCommand.class,
            ConfigCommand.class,
            CommandLine.HelpCommand.class,
        }
)
public class Jikkou {

    static LocalDateTime START_TIME;

    @Mixin
    public CLIOptionsMixin options;

    public static void main(final String... args) {
        START_TIME = LocalDateTime.now();
        final Jikkou command = new Jikkou();
        final CommandLine commandLine = new CommandLine(command)
                .setUsageHelpWidth(160)
                .setExecutionStrategy(new CommandLine.RunLast(){
                    @Override
                    public int execute(final CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException {
                        // Initialization must be triggered after args was parsed by Picocli
                        command.initialize();
                        return super.execute(parseResult);
                    }
                })
                .setExecutionExceptionHandler((ex, cmd, parseResult) -> {
                    final PrintWriter err = cmd.getErr();
                    if (! (ex instanceof JikkouException) ) {
                        err.println(cmd.getColorScheme().stackTraceText(ex));
                    }
                    err.println(cmd.getColorScheme().errorText(ex.getMessage()));
                    return cmd.getCommandSpec().exitCodeOnExecutionException();
                })
                .setParameterExceptionHandler(new ShortErrorMessageHandler());

        if (args.length > 0) {
            final int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } else {
            commandLine.usage(System.out);
        }
    }

    /**
     * Initializes the global-static configuration object.
     */
    private void initialize() {
        Map<String, Object> adminClientParams = new HashMap<>();
        if (options.clientCommandConfig != null) {
            final Properties cliCommandProps = PropertiesUtils.loadPropertiesConfig(options.clientCommandConfig);
            adminClientParams.putAll(PropertiesUtils.toMap(cliCommandProps));
        }

        adminClientParams.putAll(options.clientCommandProperties);
        if (options.bootstrapServer != null && !options.bootstrapServer.isEmpty()) {
            adminClientParams.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, options.bootstrapServer);
        }

        Map<String, Object> cliConfigParams = new HashMap<>();
        cliConfigParams.put(JikkouParams.ADMIN_CLIENT_CONFIG_NAME, adminClientParams);

        JikkouConfig.builder()
                .withConfigFile(options.configFile)
                .withConfigOverrides(cliConfigParams)
                .getOrCreate();
    }

    public static class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public int handleParseException(final CommandLine.ParameterException ex,
                                        final String[] args) {
            CommandLine cmd = ex.getCommandLine();
            PrintWriter err = cmd.getErr();

            // if tracing at DEBUG level, show the location of the issue
            if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
                err.println(cmd.getColorScheme().stackTraceText(ex));
            }

            err.println(ex.getMessage());
            CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
            CommandSpec spec = cmd.getCommandSpec();
            cmd.usage(err);
            err.printf("%nSee '%s --help' for more information about a command.%n", spec.qualifiedName());
            return cmd.getExitCodeExceptionMapper() != null
                    ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                    : spec.exitCodeOnInvalidInput();
        }
    }

    static String getExecutionTime() {
        final long execTimeInMillis = Duration.between (START_TIME, LocalDateTime.now ()).toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(execTimeInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(execTimeInMillis) % 60;
        long milliseconds = execTimeInMillis % 1000;

        if (minutes == 0) {
            return seconds == 0 ?
                String.format ("%dms", milliseconds) :
                String.format ("%ds %dms", seconds, milliseconds);
        }
        return String.format("%dmin %ds %dms", minutes, seconds, milliseconds);
    }

    /**
     * Returns version information from jar file's {@code /META-INF/MANIFEST.MF} file.
     */
    static class ManifestVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() throws Exception {
            Enumeration<URL> resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    Manifest manifest = new Manifest(url.openStream());
                    if (isApplicableManifest(manifest)) {
                        Attributes attr = manifest.getMainAttributes();
                        return new String[] {
                            get(attr, "Implementation-Title") + " version v" + get(attr, "Implementation-Version")
                        };
                    }
                } catch (IOException ex) {
                    return new String[] { "Unable to read from " + url + ": " + ex };
                }
            }
            return new String[0];
        }

        private boolean isApplicableManifest(Manifest manifest) {
            Attributes attributes = manifest.getMainAttributes();
            return Jikkou.class.getSimpleName().equals(get(attributes, "Implementation-Title"));
        }

        private static Object get(Attributes attributes, String key) {
            return attributes.get(new Attributes.Name(key));
        }
    }
}
