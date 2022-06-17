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
package io.streamthoughts.jikkou.cli.command.extensions;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionFactory;
import io.streamthoughts.jikkou.io.Jackson;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "extensions",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "List all extensions available.",
        description = "This command can be used to display all extensions classes available.",
        mixinStandardHelpOptions = true)
public class ExtensionCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--built-in",
            defaultValue = "false",
            description = "List of available extensions, including those that are built-in.")
    public boolean builtIn;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        final List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(JikkouConfig.get())
                .getOrElse(Collections.emptyList());

        var registry = new ReflectiveExtensionFactory()
                .addExtensionPaths(extensionPaths);

        if (builtIn) {
            // Scan all sub-packages of the root package of Jikkou API for declared extensions.
            registry.addRootApiPackage();
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Jackson.JSON_OBJECT_MAPPER.writeValue(baos, registry.allExtensionTypes());
            System.out.println(baos);
            return CommandLine.ExitCode.OK;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
