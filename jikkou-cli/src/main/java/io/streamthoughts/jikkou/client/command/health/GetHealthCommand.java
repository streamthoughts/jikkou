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
package io.streamthoughts.jikkou.client.command.health;

import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.extensions.ExtensionDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.health.Health;
import io.streamthoughts.jikkou.api.health.HealthAggregator;
import io.streamthoughts.jikkou.api.health.HealthIndicator;
import io.streamthoughts.jikkou.api.health.Status;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.client.ClientContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "get",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Print health indicators.",
        description = "This command can be used to get information about the health of target environments.",
        mixinStandardHelpOptions = true)
public class GetHealthCommand implements Callable<Integer> {

    enum Formats { JSON, YAML }

    @Option(names = { "--output", "-o" },
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Allowed values: json, yaml (default yaml)."
    )
    public Formats format;

    @Option(names = "--timeout-ms",
            defaultValue = "2000",
            description = "Timeout in milliseconds for retrieving health indicators (default: 2000).")
    public long timeoutMs;

    @Parameters(
            paramLabel = "HEALTH_INDICATOR",
            description = "Name of the health indicator (use 'all' to get all indicators).")
    String indicator;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        ClientContext clientContext = ClientContext.get();

        ExtensionFactory factory = clientContext.getExtensionFactory();

        Health health = null;
        if (indicator.equalsIgnoreCase("all")) {
            List<Health> l = factory
                    .getAllExtensions(HealthIndicator.class, clientContext.getConfiguration())
                    .stream().map(i -> i.getHealth(Duration.ofMillis(timeoutMs)))
                    .toList();
            health = new HealthAggregator().aggregate(l);
        } else {
            ExtensionDescriptor<HealthIndicator> descriptor = factory.getAllDescriptorsForType(HealthIndicator.class)
                    .stream()
                    .filter(it -> it.name().equalsIgnoreCase(indicator))
                    .findFirst()
                    .orElseThrow(() ->
                            new JikkouRuntimeException("Cannot find health-indicator for name '" + indicator + "'")
                    );
            HealthIndicator indicator = factory.getExtension(descriptor.clazz(), clientContext.getConfiguration());
            health = indicator.getHealth(Duration.ofMillis(timeoutMs));
        }

        if (health != null) {

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                switch (format) {
                    case JSON -> Jackson.JSON_OBJECT_MAPPER.writeValue(baos, health);
                    case YAML -> Jackson.YAML_OBJECT_MAPPER.writeValue(baos, health);
                }
                System.out.println(baos);
            }

            return health.getStatus().equals(Status.UP) ?
                    CommandLine.ExitCode.OK :
                    CommandLine.ExitCode.SOFTWARE;
        }

        return CommandLine.ExitCode.OK;
    }
}
