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
package io.streamthoughts.kafka.specs.command.quotas.subcommands;

import io.streamthoughts.kafka.specs.command.BaseCommand;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.io.YAMLSpecWriter;
import io.streamthoughts.kafka.specs.manager.DescribeOptions;
import io.streamthoughts.kafka.specs.manager.KafkaQuotaManager;
import io.streamthoughts.kafka.specs.manager.adminclient.AdminClientKafkaQuotaManager;
import io.streamthoughts.kafka.specs.model.MetaObject;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

@CommandLine.Command(name = "describe",
        description = "Describe quotas that currently exist on the remote Kafka cluster."
)
public class Describe extends BaseCommand {

    @CommandLine.Option(names = "--output-file",
            description = "Writes the result of the command to this file instead of stdout."
    )
    File outputFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        try {
            KafkaQuotaManager manager = new AdminClientKafkaQuotaManager();
            manager.configure(JikkouConfig.get());

            final OutputStream os = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;
            final List<V1QuotaObject> quotas = manager.describe(new DescribeOptions() {});
            final V1SpecsObject specsObject = V1SpecsObject.withQuotas(quotas);
            YAMLSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), specsObject), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
