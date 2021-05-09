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

import io.streamthoughts.kafka.specs.ClusterSpec;
import io.streamthoughts.kafka.specs.ClusterSpecReader;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.Printer;
import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public abstract class WithSpecificationCommand<T> extends BaseCommand {

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    @ArgGroup(multiplicity = "1")
    FileOptions specOptions;

    static class FileOptions {
        @Option(names = "--file-path",
                description = "Align cluster resources with the specified specifications."
        )
        File file;
        @Option(names = "--file-url",
                description = "Delete all remote entities which are not described in specifications."
        )
        URL url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call(final AdminClient adminClient) {
        final Collection<OperationResult<T>> results = executeCommand(adminClient);
        Printer.print(results, execOptions.verbose);
        return CommandLine.ExitCode.OK;
    }

    public abstract Collection<OperationResult<T>> executeCommand(final AdminClient adminClient);

    public boolean isDryRun() {
        return execOptions.dryRun;
    }

    public ClusterSpec clusterSpec() {

        if (specOptions.url != null) {
            try {
                return READER.read(specOptions.url.openStream());
            } catch (IOException e) {
                throw new RuntimeException("Can't open specification from URL '" + specOptions.url + "'.");
            }
        }

        try {
            if (specOptions.file != null) {
                return READER.read(new FileInputStream(specOptions.file));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Can't open specification from file '" + specOptions.file + "'.");
        }
        throw new IllegalArgumentException("no specification");
    }
}
