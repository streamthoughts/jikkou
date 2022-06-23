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
package io.streamthoughts.jikkou.client.command.extensions;

import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionFactory;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.client.JikkouConfigProperty;
import io.streamthoughts.jikkou.client.JikkouContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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

        List<String> extensionPaths = JikkouConfigProperty.EXTENSION_PATHS
                .getOptional(JikkouContext.jikkouConfig())
                .orElse(Collections.emptyList());

        // Create a new ReflectiveExtensionFactory for the user-defined extensions paths.
        var extensionFactory = new ReflectiveExtensionFactory()
                .addExtensionPaths(extensionPaths);

        if (builtIn) {
            // Scan all sub-packages of the root package of Jikkou API for declared extensions.
            extensionFactory.addRootApiPackage();
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Jackson.JSON_OBJECT_MAPPER.writeValue(baos, extensionFactory.allExtensionTypes());
            System.out.println(baos);
            return CommandLine.ExitCode.OK;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
