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

import static picocli.CommandLine.Model.CommandSpec;

import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.client.banner.Banner;
import io.streamthoughts.jikkou.client.banner.BannerPrinterBuilder;
import io.streamthoughts.jikkou.client.banner.JikkouBanner;
import io.streamthoughts.jikkou.client.command.ApplyResourceCommand;
import io.streamthoughts.jikkou.client.command.CreateResourceCommand;
import io.streamthoughts.jikkou.client.command.DeleteResourceCommand;
import io.streamthoughts.jikkou.client.command.DiffCommand;
import io.streamthoughts.jikkou.client.command.ResourcesCommand;
import io.streamthoughts.jikkou.client.command.UpdateResourceCommand;
import io.streamthoughts.jikkou.client.command.ValidateCommand;
import io.streamthoughts.jikkou.client.command.config.ConfigCommand;
import io.streamthoughts.jikkou.client.command.config.ContextNamesCompletionCandidateCommand;
import io.streamthoughts.jikkou.client.command.extension.ExtensionCommand;
import io.streamthoughts.jikkou.client.command.get.GetCommandGenerator;
import io.streamthoughts.jikkou.client.command.health.HealthCommand;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * The main-class
 */
@Command(name = "jikkou",
        descriptionHeading = "%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        headerHeading = "Usage: ",
        synopsisHeading = "%n",
        description = "Jikkou streamlines the management of the configurations that live on your data streams platform.%n%nFind more information at: https://streamthoughts.github.io/jikkou/.",
        mixinStandardHelpOptions = true,
        versionProvider = Jikkou.ManifestVersionProvider.class,
        subcommands = {
                CreateResourceCommand.class,
                DeleteResourceCommand.class,
                UpdateResourceCommand.class,
                ApplyResourceCommand.class,
                ResourcesCommand.class,
                ExtensionCommand.class,
                ConfigCommand.class,
                DiffCommand.class,
                ValidateCommand.class,
                HealthCommand.class,
                CommandLine.HelpCommand.class,
                AutoComplete.GenerateCompletion.class,
                ContextNamesCompletionCandidateCommand.class
        }
)
public final class Jikkou {

    private static final Logger LOG = LoggerFactory.getLogger(Jikkou.class);

    static LocalDateTime START_TIME;

    private Jikkou() {}

    public static void main(final String... args) {
        START_TIME = LocalDateTime.now();
        var printer = BannerPrinterBuilder.newBuilder()
                .setLogger(LOG)
                .setLoggerLevel(Level.INFO)
                .setMode(Banner.Mode.LOG)
                .build();
        printer.print(new JikkouBanner());

        final Jikkou command = new Jikkou();
        final CommandLine commandLine = new CommandLine(command)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setUsageHelpWidth(160)
                .setExecutionStrategy(new CommandLine.RunLast() {
                    @Override
                    public int execute(final CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException {
                        // Initialization must be triggered after args was parsed by Picocli
                        return super.execute(parseResult);
                    }
                })
                .setExecutionExceptionHandler((ex, cmd, parseResult) -> {
                    final PrintWriter err = cmd.getErr();
                    if (!(ex instanceof JikkouRuntimeException)) {
                        err.println(cmd.getColorScheme().stackTraceText(ex));
                    }
                    String message = ex.getLocalizedMessage() != null ?
                            ex.getLocalizedMessage() :
                            ex.getClass().getName();

                    err.println(cmd.getColorScheme().errorText("Error: " + message));
                    return cmd.getCommandSpec().exitCodeOnExecutionException();
                })
                .setParameterExceptionHandler(new ShortErrorMessageHandler());


        commandLine.addSubcommand(new GetCommandGenerator().createGetCommandLine());

        CommandLine gen = commandLine.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(true);

        if (args.length > 0) {
            final int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } else {
            commandLine.usage(System.out);
        }
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
            if ("DEBUG" .equalsIgnoreCase(System.getProperty("picocli.trace"))) {
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

    public static long getExecutionTime() {
        return Duration.between(START_TIME, LocalDateTime.now()).toMillis();
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
                        return new String[]{
                                get(attr, "Implementation-Title") + " version v" + get(attr, "Implementation-Version")
                        };
                    }
                } catch (IOException ex) {
                    return new String[]{"Unable to read from " + url + ": " + ex};
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
