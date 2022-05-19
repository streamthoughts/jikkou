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
package io.streamthoughts.jikkou.cli.command.quotas.subcommands;

import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.cli.command.BaseCommand;
import io.streamthoughts.jikkou.io.YAMLSpecWriter;
import io.streamthoughts.jikkou.api.manager.DescribeOptions;
import io.streamthoughts.jikkou.api.manager.KafkaQuotaManager;
import io.streamthoughts.jikkou.api.model.MetaObject;
import io.streamthoughts.jikkou.api.model.V1QuotaObject;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.config.JikkouConfig;
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
            final KafkaQuotaManager manager = JikkouParams.KAFKA_QUOTAS_MANAGER.get(JikkouConfig.get());

            final OutputStream os = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;
            final List<V1QuotaObject> quotas = manager.describe(new DescribeOptions() {});
            final V1SpecObject specsObject = V1SpecObject.withQuotas(quotas);
            YAMLSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), specsObject), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
