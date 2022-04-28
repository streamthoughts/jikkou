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
package io.streamthoughts.jikkou.kafka.command.broker.subcommands;

import io.streamthoughts.jikkou.kafka.config.JikkouConfig;
import io.streamthoughts.jikkou.kafka.model.V1SpecObject;
import io.streamthoughts.jikkou.kafka.command.BaseCommand;
import io.streamthoughts.jikkou.kafka.io.YAMLSpecWriter;
import io.streamthoughts.jikkou.kafka.manager.BrokerDescribeOptions;
import io.streamthoughts.jikkou.kafka.manager.KafkaBrokerManager;
import io.streamthoughts.jikkou.kafka.manager.adminclient.AdminClientKafkaBrokerManager;
import io.streamthoughts.jikkou.kafka.model.MetaObject;
import io.streamthoughts.jikkou.kafka.model.V1BrokerObject;
import io.streamthoughts.jikkou.kafka.model.V1SpecFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

@Command(name = "describe",
        description = "Describe all the Broker's configuration on remote cluster."
)
public class Describe extends BaseCommand {

    @Option(names = {"--default-configs"},
            description = "Export built-in default configuration for configs that have a default value."
    )
    boolean describeDefaultConfigs;

    @Option(names = {"--static-broker-configs"},
            defaultValue = "true",
            description = "Export static configs provided as broker properties at start up (e.g. server.properties file)."
    )
    boolean describeStaticBrokerConfigs;

    @Option(names = {"--dynamic-broker-configs"},
            defaultValue = "true",
            description = "Export dynamic configs that is configured as default for all brokers or for specific broker in the cluster."
    )
    boolean describeDynamicBrokerConfigs;

    @Option(names = "--file-path",
            description = "The file path to write the description of Topics."
    )
    File filePath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {

        final KafkaBrokerManager manager = new AdminClientKafkaBrokerManager(JikkouConfig.get());

        final BrokerDescribeOptions options = new BrokerDescribeOptions()
                .withDescribeDefaultConfigs(describeDefaultConfigs)
                .withDescribeDynamicBrokerConfigs(describeDynamicBrokerConfigs)
                .withDescribeStaticBrokerConfigs(describeStaticBrokerConfigs);

        List<V1BrokerObject> resources = manager.describe(options);

        try {
            OutputStream os = (filePath != null) ? new FileOutputStream(filePath) : System.out;

            final V1SpecObject specsObject = V1SpecObject.withBrokers(resources);
            YAMLSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), specsObject), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
