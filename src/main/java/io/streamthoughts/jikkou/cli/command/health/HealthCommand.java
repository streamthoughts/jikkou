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
package io.streamthoughts.jikkou.cli.command.health;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionFactory;
import io.streamthoughts.jikkou.api.health.Health;
import io.streamthoughts.jikkou.api.health.HealthAggregator;
import io.streamthoughts.jikkou.api.health.HealthIndicator;
import io.streamthoughts.jikkou.api.health.Status;
import io.streamthoughts.jikkou.io.Jackson;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "health",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Print health indicators about the target environment.",
        description = "This command can be used to get information about the health of the target environment .",
        mixinStandardHelpOptions = true)
public class HealthCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--timeout-ms",
            defaultValue = "2000",
            description = "Timeout in milliseconds for retrieving health indicators (default: 2000)")
    public long timeoutMs;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {

        List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(JikkouConfig.get())
                .getOrElse(Collections.emptyList());

        var factory = new ReflectiveExtensionFactory()
                .addRootApiPackage()
                .addExtensionPaths(extensionPaths);

        Collection<Health> healths = factory
                .getAllExtensions(HealthIndicator.class, JikkouConfig.get())
                .stream().map(i -> i.getHealth(Duration.ofMillis(timeoutMs)))
                .toList();

        Health aggregate = new HealthAggregator().aggregate(healths);
        Jackson.JSON_OBJECT_MAPPER.writeValue(System.out, aggregate);

        return aggregate.getStatus().equals(Status.UP) ?
                CommandLine.ExitCode.OK :
                CommandLine.ExitCode.SOFTWARE;
    }
}
