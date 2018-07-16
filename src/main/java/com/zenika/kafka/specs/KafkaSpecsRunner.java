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
package com.zenika.kafka.specs;

import com.zenika.kafka.specs.command.ClusterCommand;
import com.zenika.kafka.specs.command.ExecuteTopicCommand;
import com.zenika.kafka.specs.command.ExportTopicsCommand;
import com.zenika.kafka.specs.internal.AdminClientUtils;
import org.apache.kafka.clients.admin.AdminClient;

/**
 * The command line main class.
 */
public class KafkaSpecsRunner {

    public static void main(String[] args) {

        final KafkaSpecsRunnerOptions options = new KafkaSpecsRunnerOptions(args);

        if(args.length == 0) {
            CLIUtils.printUsageAndDie(options.parser, "Create, Alter, Delete, Describe or clean Kafka cluster resources");
        }

        if(!options.hasSingleAction()) {
            CLIUtils.printUsageAndDie(options.parser, "Command must include exactly one action: --execute, --describe, --clean or --diff");
        }

        options.checkArgs();

        int exitCode = 0;
        try (AdminClient client = AdminClientUtils.newAdminClient(options)) {

            if (options.isExecuteCommand() ) {
                CLIUtils.askToProceed();
                ExecuteTopicCommand command = new ExecuteTopicCommand();
                Printer.printAndExit(command.execute(options, client), options.verbose());
            }

            if (options.isExportCommand()) {
                ClusterCommand command = new ExportTopicsCommand();
                command.execute(options, client);
            }

            if (options.isDiffCommand()|| options.isCleanAllCommand()) {
                System.err.println("Command not supported yet!");
                exitCode = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            exitCode = 1;
        } finally {
            System.exit(exitCode);
        }
    }
}
