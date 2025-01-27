/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import static picocli.CommandLine.Model.CommandSpec;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.streamthoughts.jikkou.client.banner.Banner;
import io.streamthoughts.jikkou.client.banner.BannerPrinterBuilder;
import io.streamthoughts.jikkou.client.banner.JikkouBanner;
import io.streamthoughts.jikkou.client.command.DiffCommand;
import io.streamthoughts.jikkou.client.command.PrepareCommand;
import io.streamthoughts.jikkou.client.command.action.ActionCommandLineFactory;
import io.streamthoughts.jikkou.client.command.config.ConfigCommand;
import io.streamthoughts.jikkou.client.command.config.ContextNamesCompletionCandidateCommand;
import io.streamthoughts.jikkou.client.command.extension.ApiExtensionCommand;
import io.streamthoughts.jikkou.client.command.get.GetCommandLineFactory;
import io.streamthoughts.jikkou.client.command.health.HealthCommand;
import io.streamthoughts.jikkou.client.command.reconcile.ApplyResourceCommand;
import io.streamthoughts.jikkou.client.command.reconcile.CreateResourceCommand;
import io.streamthoughts.jikkou.client.command.reconcile.DeleteResourceCommand;
import io.streamthoughts.jikkou.client.command.reconcile.PatchResourceCommand;
import io.streamthoughts.jikkou.client.command.reconcile.UpdateResourceCommand;
import io.streamthoughts.jikkou.client.command.resources.ListApiResourcesCommand;
import io.streamthoughts.jikkou.client.command.server.ServerInfoCommand;
import io.streamthoughts.jikkou.client.command.validate.ValidateCommand;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import io.streamthoughts.jikkou.client.renderer.CommandGroupRenderer;
import io.streamthoughts.jikkou.core.JikkouInfo;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.InvalidResourceFileException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.http.client.JikkouApiClient;
import io.streamthoughts.jikkou.rest.data.Info;
import jakarta.inject.Singleton;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

/**
 * The main-class
 */
@Command(name = "jikkou",
        descriptionHeading = "%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOPTIONS:%n%n",
        commandListHeading = "%nCommands:%n%n",
        headerHeading = "Usage: ",
        synopsisHeading = "%n",
        description = "Jikkou CLI:: A command-line client designed to provide an efficient and easy way to manage, automate, and provision resources. %n%nFind more information at: https://www.jikkou.io/.",
        mixinStandardHelpOptions = true,
        versionProvider = Jikkou.ResourcePropertiesVersionProvider.class,
        subcommands = {
                CreateResourceCommand.class,
                DeleteResourceCommand.class,
                UpdateResourceCommand.class,
                ApplyResourceCommand.class,
                PatchResourceCommand.class,
                ApiExtensionCommand.class,
                ListApiResourcesCommand.class,
                ConfigCommand.class,
                DiffCommand.class,
                PrepareCommand.class,
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

    @Mixin
    LoggingMixin loggingMixin;

    private static LocalDateTime START_TIME;

    @EventListener
    public void onStartupEvent(StartupEvent event) {
        ConfigurationContext configurationContext = GlobalConfigurationContext.getConfigurationContext();
        if (!configurationContext.isExists()) {
            System.err.printf(
                    "No configuration context has been defined (file:%s)." +
                            " Run 'jikkou config set-context <context_name> --config-props=kafka.client.bootstrap.servers=localhost:9092" +
                            " [--config-props=<config_string>] [--config-file=<config_file>].' to create a context.%n",
                    configurationContext.getConfigFile());
        }
    }

    public static void main(final String... args) {
        Logging.configureRootLoggerLevel();
        var printer = BannerPrinterBuilder.newBuilder()
                .setLogger(LOG)
                .setLoggerLevel(Level.INFO)
                .setMode(Banner.Mode.LOG)
                .build();
        printer.print(new JikkouBanner());

        System.exit(execute(args));
    }

    public static int execute(String[] args) {
        START_TIME = LocalDateTime.now();
        Configuration config = GlobalConfigurationContext.getConfiguration();
        ApplicationContextBuilder builder = ApplicationContext.builder(Jikkou.class, Environment.CLI);
        // Inject config to Micronaut to enable conditional beans.
        builder.propertySources(new JikkouPropertySource(config));
        try (ApplicationContext context = builder.start()) {
            final CommandLine commandLine = createCommandLine(context);
            if (args.length > 0) {
                return commandLine.execute(args);
            } else {
                commandLine.usage(System.out);
                return CommandLine.ExitCode.USAGE;
            }
        }
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        loggingMixin.configureRootLoggerLevel();
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }

    @NotNull
    private static CommandLine createCommandLine(ApplicationContext context) {
        Jikkou app = context.getBean(Jikkou.class);
        final CommandLine commandLine = new CommandLine(app, new MicronautFactory(context))
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setUsageHelpWidth(160)
            .setExecutionStrategy(app::executionStrategy)
            .setExecutionExceptionHandler((ex, cmd, parseResult) -> {
                final PrintWriter err = cmd.getErr();
                err.println(cmd.getColorScheme().stackTraceText(ex));
                err.println(cmd.getColorScheme().errorText(String.format("Error: %s: %s",
                        ex.getClass().getSimpleName(),
                        ex.getLocalizedMessage()
                )));
                if (ex instanceof InvalidResourceFileException) {
                    return CommandLine.ExitCode.USAGE;
                }
                return cmd.getCommandSpec().exitCodeOnExecutionException();
            })
            .setParameterExceptionHandler(new ShortErrorMessageHandler());

        Optional<JikkouApiClient> optionalApiClient = context.findBean(JikkouApiClient.class);
        // NOT IN PROXY-MODE
        boolean isApiEnabled = true;
        boolean isProxyMode = optionalApiClient.isPresent();
        if (optionalApiClient.isPresent()) {
            // Add additional command 'server-info'
            commandLine.addSubcommand(context.getBean(ServerInfoCommand.class));
            JikkouApiClient client = optionalApiClient.get();
            try {
                Info info = client.getServerInfo();
                LOG.info("Connected to Jikkou API server (version: %{}, buildTimestamp: {}, commitId: {}).",
                        info.version(),
                        info.buildTimestamp(),
                        info.commitId()
                );
            } catch (Exception e) {
                final PrintWriter err = commandLine.getErr();
                err.println(commandLine.getColorScheme().errorText(String.format("Error: %s. Cause: %s",
                        "Failed to connect to Jikkou API server",
                        e.getLocalizedMessage()
                )));
                isApiEnabled = false; // DISABLE API
            }
        }

        if (isApiEnabled) {
            try {
                commandLine.addSubcommand(context.getBean(GetCommandLineFactory.class).createCommandLine());
                commandLine.addSubcommand(context.getBean(ActionCommandLineFactory.class).createCommandLine());
            } catch (Exception e) {
                LOG.error("Cannot generate 'get/action' subcommands.", e);
                System.err.println("Error: Cannot generate 'get/action' subcommands. Cause: " + e.getLocalizedMessage());
            }
        }

        CommandLine gen = commandLine.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(false);

        commandLine.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);

        CommandLine.IHelpSectionRenderer renderer = buildHelpSectionRenderer(isProxyMode, isApiEnabled);
        commandLine.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        return commandLine;
    }

    public static CommandLine.IHelpSectionRenderer buildHelpSectionRenderer(boolean isProxyMode,
                                                                            boolean isApiEnabled) {

        Map<String, List<String>> sections = new LinkedHashMap<>();
        List<String> core = new ArrayList<>();
        if (isApiEnabled) {
            // All COMMANDS in alphabetic order.
            core.add("apply");
            core.add("create");
            core.add("delete");
            core.add("diff");
            core.add("get");
            core.add("patch");
            core.add("prepare");
            core.add("update");
            core.add("validate");
        }

        List<String> system = new ArrayList<>();
        if (isApiEnabled) {
            system.add("health");
            system.add("action");
            if (isProxyMode) system.add("server-info");
        }

        List<String> additional = new ArrayList<>();

        if (isApiEnabled) {
            additional.add("api-resources");
            additional.add("api-extensions");
        }

        additional.add("config");
        additional.add("generate-completion");
        additional.add("help");

        sections.put("%nCORE COMMANDS:%n",
                core.stream().sorted(Comparator.comparing(Function.identity())).toList());
        sections.put("%nSYSTEM MANAGEMENT COMMANDS:%n",
                system.stream().sorted(Comparator.comparing(Function.identity())).toList());
        sections.put("%nADDITIONAL COMMANDS:%n",
                additional.stream().sorted(Comparator.comparing(Function.identity())).toList());

        return new CommandGroupRenderer(sections);
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
