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
package io.streamthoughts.jikkou.client;

import java.io.Console;
import org.apache.kafka.common.utils.Exit;
import picocli.CommandLine;

/**
 * Utility class to handle arguments.
 */
public class CLIUtils {


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
