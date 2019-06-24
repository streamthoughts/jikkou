/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class KafkaSpecsRunnerOptions {

    private static final String ExecuteCommandArg       = "execute";
    private static final String ExecuteCommandDoc       = "COMMAND: Align cluster resources with the specified specifications";

    private static final String AlterCommandArg         = "alter";
    private static final String AlterCommandDoc         = "OPTION : Alter all existing entities that have configuration changed";

    private static final String CreateCommandArg        = "create";
    private static final String CreateCommandDoc        = "OPTION : Create all entities that currently do not exist on remote cluster";

    private static final String DeleteExecuteArg        = "delete";
    private static final String DeleteExecuteDoc        = "OPTION : Delete all remote entities which are not described in specifications";

    private static final String DryRunExecuteArg        = "dry-run";
    private static final String DryRunExecuteDoc        = "OPTION : Execute command in Dry-Run mode";

    private static final String EntityTypeArg           = "entity-type";
    private static final String EntityTypeDoc           = "OPTION : entity on which to execute command [topics|users]";

    private static final String TopicsExecuteArg        = "topics";
    private static final String TopicsExecuteDoc        = "OPTION : Only run command for this of topics (separated by ,)";

    private static final String DefaultConfigArg        = "default-configs";
    private static final String DefaultConfigDoc        = "OPTION : Export built-in default configuration for configs that have a default value";

    private static final String DiffCommandArg          = "diff";
    private static final String DiffCommandDoc          = "COMMAND: Display difference between cluster resources and the specified specifications";

    private static final String ExportCommandArg        = "describe";
    private static final String ExportCommandDoc        = "COMMAND: Describe resources configuration of a specified cluster";

    private static final String CleanAllCommandArg      = "clean-all";
    private static final String CleanAllCommandDoc      = "COMMAND: Temporally set retention.ms to 0 in order to delete messages for each topic";

    private static final String BootstrapServerArg      = "bootstrap-server";
    private static final String BootstrapServerDoc      = "REQUIRED: The server to connect to.";

    private static final String CommandConfigArg        = "command.config";
    private static final String CommandConfigDoc        = "A property file containing configs to be passed to Admin Client.";

    private static final String CommandPropertiesArgs   = "command-property";

    private static final String ClusterSpecificationArg = "file";
    private static final String ClusterSpecFileDoc      = "The cluster specification to used for the command.";

    private static final String VerboseArg              = "verbose";
    private static final String VerboseDoc              = "Print resources details";

    private static final String AssumeYesArg            = "yes";
    private static final String AssumeYesDoc            = "Assume yes; assume that the answer to any question which would be asked is yes. ";

    private final OptionSet options;

    private final ArgumentAcceptingOptionSpec<File> commandConfigOpt;

    private final ArgumentAcceptingOptionSpec<String> commandPropsOpt;

    private final ArgumentAcceptingOptionSpec<String> bootstrapServerOpt;

    private final ArgumentAcceptingOptionSpec<File> clusterSpecificationFileOpt;

    private final ArgumentAcceptingOptionSpec<String> topics;

    public final OptionParser parser;

    private final Set<OptionSpec> actions;

    private final OptionSpec[] executeOpts;

    private ArgumentAcceptingOptionSpec<String> entityTypes;

    /**
     * Creates a new {@link KafkaSpecsRunnerOptions} instance.
     *
     * @param args  the arguments
     */
     KafkaSpecsRunnerOptions(String[] args) {
        this.parser = new OptionParser(false);

        commandPropsOpt = parser.accepts(CommandPropertiesArgs, CommandConfigDoc)
                .withRequiredArg()
                .describedAs("command config property")
                .ofType(String.class);

        commandConfigOpt = parser.accepts(CommandConfigArg, CommandConfigDoc)
                .withRequiredArg()
                .describedAs("command config property file")
                .ofType(File.class);

        bootstrapServerOpt = parser.accepts(BootstrapServerArg, BootstrapServerDoc)
                .withRequiredArg()
                .describedAs("server(s) to use for bootstrapping")
                .ofType(String.class);

         entityTypes = parser.accepts(EntityTypeArg, EntityTypeDoc)
                 .withRequiredArg()
                 .ofType(String.class)
                 .withValuesSeparatedBy(",");

        // commands
        OptionSpecBuilder executeSpecBuilder = parser.accepts(ExecuteCommandArg, ExecuteCommandDoc);
        OptionSpecBuilder exportSpecBuilder = parser.accepts(ExportCommandArg, ExportCommandDoc);
        OptionSpecBuilder cleanAllSpecBuilder = parser.accepts(CleanAllCommandArg, CleanAllCommandDoc);
        OptionSpecBuilder diffSpecBuilder = parser.accepts(DiffCommandArg, DiffCommandDoc);

        actions = new HashSet<>();
        actions.add(executeSpecBuilder);
        actions.add(exportSpecBuilder);
        actions.add(cleanAllSpecBuilder);
        actions.add(diffSpecBuilder);


        // options for executes
        OptionSpecBuilder create = parser.accepts(CreateCommandArg, CreateCommandDoc);
        OptionSpecBuilder delete = parser.accepts(DeleteExecuteArg, DeleteExecuteDoc);
        OptionSpecBuilder alter = parser.accepts(AlterCommandArg, AlterCommandDoc);

        executeOpts = new OptionSpec[]{create, delete, alter};

        topics = parser.accepts(TopicsExecuteArg, TopicsExecuteDoc)
                .withRequiredArg().ofType(String.class)
                .withValuesSeparatedBy(",");

        clusterSpecificationFileOpt = parser.accepts(ClusterSpecificationArg, ClusterSpecFileDoc)
                .withRequiredArg()
                .ofType(File.class);

        parser.accepts(DryRunExecuteArg, DryRunExecuteDoc);
        parser.accepts(DefaultConfigArg, DefaultConfigDoc);
        parser.accepts(VerboseArg, VerboseDoc);
        parser.accepts(AssumeYesArg, AssumeYesDoc);

        parser.mutuallyExclusive(executeSpecBuilder, diffSpecBuilder, exportSpecBuilder, cleanAllSpecBuilder);

        parser.accepts( "help", "Print usage information." ).forHelp();

        options = parser.parse(args);
    }

    public void checkArgs() {
        CLIUtils.checkRequiredArgs(parser, options, bootstrapServerOpt);
        if (isExecuteCommand()) {
            CLIUtils.checkRequiredArgs(parser, options, clusterSpecificationFileOpt);
            CLIUtils.checkRequiredArgs(parser, options, entityTypes);
            CLIUtils.checkOnceRequiredArgs(parser, options, executeOpts);
        }
    }

    public Collection<EntityType> entityTypes() {
        return (options.has(entityTypes)) ?
                entityTypes.values(options).stream().map(EntityType::from).collect(Collectors.toList()) :
                Collections.emptyList();
    }

    public boolean isAssumeYes() {
         return options.has(AssumeYesArg);
    }

    public boolean hasSingleAction() {
        return actions.stream().filter(options::has).count() == 1;
    }

    public boolean isExecuteCommand() {
        return options.has(ExecuteCommandArg);
    }

    public boolean verbose() {
        return options.has(VerboseArg);
    }

    public boolean isExportCommand() {
        return options.has(ExportCommandArg);
    }

    public boolean isDiffCommand() {
        return options.has(DiffCommandArg);
    }

    public boolean isCleanAllCommand() {
        return options.has(CleanAllCommandArg);
    }

    public boolean isAlterTopicsCommand() {
        return options.has(AlterCommandArg);
    }

    public boolean isDeleteTopicsCommand() {
        return options.has(DeleteExecuteArg);
    }

    public boolean isCreateTopicsCommand() {
        return options.has(CreateCommandArg);
    }

    public boolean isDryRun() {
        return options.has(DryRunExecuteArg);
    }

    public boolean isDefaultConfigs() {
        return options.has(DefaultConfigArg);
    }

    public Collection<String> topics() {
        return (options.has(topics)) ? topics.values(options) : Collections.emptyList();
    }

    public String bootstrapServerOpt() {
        return this.bootstrapServerOpt.value(options);
    }

    public File configPropsFileOpt() {
        return options.has(commandConfigOpt) ? this.commandConfigOpt.value(options) : null ;
    }

    public List<String> configPropsOpts() {
        return options.has(commandPropsOpt) ? this.commandPropsOpt.values(options) : Collections.emptyList();
    }

    public File clusterSpecificationOpt() {
        return (options.has(clusterSpecificationFileOpt)) ? clusterSpecificationFileOpt.value(options) : null;
    }
}
