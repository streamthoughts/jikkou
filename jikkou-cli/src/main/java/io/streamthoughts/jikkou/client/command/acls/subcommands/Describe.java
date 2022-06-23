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
package io.streamthoughts.jikkou.client.command.acls.subcommands;

import io.streamthoughts.jikkou.api.SimpleJikkouApi;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.client.JikkouContext;
import io.streamthoughts.jikkou.client.command.BaseCommand;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "describe",
        description = "Describe all the ACLs that currently exist on remote cluster."
)
public class Describe extends BaseCommand {

    @Option(names = "--file-path",
            description = "The file path to write the description of Topics."
    )
    File outputFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        try {
            try(SimpleJikkouApi api = JikkouContext.jikkouApi()) {
                HasMetadata resource = api.getResource(V1KafkaAuthorizationList.class);
                OutputStream os = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;

                YAMLResourceWriter.instance().write(resource, os);
                return CommandLine.ExitCode.OK;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
