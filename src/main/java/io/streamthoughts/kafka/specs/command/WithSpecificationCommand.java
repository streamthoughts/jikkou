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
package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.CLIUtils;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.ClusterSpecReader;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.Printer;
import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

public abstract class WithSpecificationCommand<T> extends BaseCommand {

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    @ArgGroup(multiplicity = "1")
    FileOptions specOptions;

    static class FileOptions {
        @Option(names = "--file-path",
                description = "The path of a file containing the specifications for Kafka resources."
        )
        File file;
        @Option(names = "--file-url",
                description = "The URL of a a file containing the specification for Kafka resources."
        )
        URL url;
    }

    @Spec
    private CommandSpec spec;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call(final AdminClient adminClient) {
        specFile(); // ensure specification is valid.
        if (!execOptions.yes) {
            CLIUtils.askToProceed(spec);
        }
        final Collection<OperationResult<T>> results = executeCommand(adminClient);
        Printer.print(results, execOptions.verbose, isDryRun());
        return CommandLine.ExitCode.OK;
    }

    public abstract Collection<OperationResult<T>> executeCommand(final AdminClient adminClient);

    public boolean isDryRun() {
        return execOptions.dryRun;
    }

    public V1SpecFile specFile() {

        final InputStream is;
        if (specOptions.url != null) {
            try {
               is = specOptions.url.openStream();
            } catch (Exception e) {
                throw new RuntimeException("Can't read specification from URL '" + specOptions.url + "': "
                        + e.getMessage());
            }
        } else if (specOptions.file != null) {
            try {
                is = new FileInputStream(specOptions.file);
            } catch (Exception e) {
                throw new RuntimeException("Can't read specification from file '" + specOptions.file + "': "
                        + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("no specification");
        }
        try {
            return READER.read(is);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
