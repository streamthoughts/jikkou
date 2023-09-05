/*
 * Copyright 2021 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ch.qos.logback.classic.LoggerContext;
import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.streamthoughts.jikkou.api.JikkouInfo;
import io.streamthoughts.jikkou.api.error.InvalidResourceFileException;
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
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
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
        description = "Jikkou CLI:: A command-line client designed to provide an efficient and easy way to manage, automate, and provision resources for any kafka infrastructure. %n%nFind more information at: https://streamthoughts.github.io/jikkou/.",
        mixinStandardHelpOptions = true,
        versionProvider = Jikkou.ResourcePropertiesVersionProvider.class,
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
@Singleton
public final class Jikkou {

    private static final Logger LOG = LoggerFactory.getLogger(Jikkou.class);

    static LocalDateTime START_TIME;

    @Inject
    private ConfigurationContext configurationContext;

    @EventListener
    public void onStartupEvent(StartupEvent event) {
        if (!configurationContext.isExists()) {
            System.err.println(
                    "No configuration context has been defined." +
                            " Run 'jikkou config set-context <context_name> --config=kafka.client.bootstrap.servers=localhost:9092" +
                            " [--client-config=<config_string>] [--config-file=<config_file>].' to create a context."
            );
        }
    }

    public static void main(final String... args) {
        setRootLogLevelWithEnv();
        START_TIME = LocalDateTime.now();
        var printer = BannerPrinterBuilder.newBuilder()
                .setLogger(LOG)
                .setLoggerLevel(Level.INFO)
                .setMode(Banner.Mode.LOG)
                .build();
        printer.print(new JikkouBanner());

        System.exit(execute(args));
    }

    private static void setRootLogLevelWithEnv() {
        String rootLogLevel = System.getenv("ROOT_LOG_LEVEL");
        if (rootLogLevel != null) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger("io.streamthoughts.jikkou");
            logger.setLevel(ch.qos.logback.classic.Level.toLevel(rootLogLevel.toUpperCase(Locale.ROOT)));
        }
    }

    public static int execute(String[] args) {
        try (ApplicationContext context = ApplicationContext.builder(
                Jikkou.class, Environment.CLI).start()) {
            final CommandLine commandLine = createCommandLine(context);
            if (args.length > 0) {
                return commandLine.execute(args);
            } else {
                commandLine.usage(System.out);
                return CommandLine.ExitCode.USAGE;
            }
        }
    }

    @NotNull
    private static CommandLine createCommandLine(ApplicationContext context) {
        final CommandLine commandLine = new CommandLine(Jikkou.class, new MicronautFactory(context))
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
                    if (ex instanceof InvalidResourceFileException) {
                        return CommandLine.ExitCode.USAGE;
                    }
                    return cmd.getCommandSpec().exitCodeOnExecutionException();
                })
                .setParameterExceptionHandler(new ShortErrorMessageHandler());


        GetCommandGenerator generator = context.getBean(GetCommandGenerator.class);
        commandLine.addSubcommand(generator.createGetCommandLine());

        CommandLine gen = commandLine.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(true);
        return commandLine;
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

    public static long getExecutionTime() {
        return Duration.between(START_TIME, LocalDateTime.now()).toMillis();
    }

    /**
     * Returns version information from resource file's {@code version.properties} file.
     */
    static class ResourcePropertiesVersionProvider implements CommandLine.IVersionProvider {
        
        public String[] getVersion() {
            return new String[]{
                    "Jikkou version \"" + JikkouInfo.getVersion() + "\" " + JikkouInfo.getBuildTimestamp(),
                    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})"
            };
        }
    }
}
