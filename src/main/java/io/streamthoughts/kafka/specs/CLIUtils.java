/*
 * Copyright 2020 StreamThoughts.
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

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.kafka.common.utils.Exit;
import picocli.CommandLine;

import java.io.Console;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to handle arguments.
 */
public class CLIUtils {

    static void checkRequiredArgs(final OptionParser parser, final OptionSet options, final OptionSpec... required) {
        for (OptionSpec arg : required) {
            if(!options.has(arg))
                printUsageAndDie(parser, "Missing required argument \"" + arg + "\"");
        }
    }

    static void checkOnceRequiredArgs(final OptionParser parser, final OptionSet options, final OptionSpec<String>... required) {
        Set<String> names = new HashSet<>();
        for (OptionSpec<String> arg : required) {
            if (!options.has(arg)) {
                List<String> flags = arg.options();
                names.add(flags.get(0));
            }
        }
        if (names.size() == required.length) {
            printUsageAndDie(parser, "Missing required argument : \"" + names + "\"");
        }
    }

    static void printUsageAndDie(final OptionParser parser, final String message) {
        System.err.println(message);
        try {
            parser.printHelpOn(System.err);
        } catch (IOException ignore) {
        }
        System.exit(1);
    }

    public static void askToProceed(final CommandLine.Model.CommandSpec spec) {
        final String description = spec.commandLine().getHelp().description();
        System.out.printf("Warning: You are about to: %n %s%n%n Are you sure you want to continue [y/n]%n", description);
        Console console = System.console();
        if (!console.readLine().equalsIgnoreCase("y")) {
            System.out.println("Ending your session");
            Exit.exit(0);
        }
    }

}
