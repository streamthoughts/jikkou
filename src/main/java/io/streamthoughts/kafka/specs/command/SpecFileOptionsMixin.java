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

import io.streamthoughts.kafka.specs.ClusterSpecReader;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class SpecFileOptionsMixin {

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    @CommandLine.Option(names = "--file-path",
            description = "Location of the file containing the specifications for Kafka resources."
    )
    File file;
    @CommandLine.Option(names = "--file-url",
            description = "Location of the file containing the specification for Kafka resources."
    )
    URL url;

    public V1SpecFile parse(@NotNull final SetOptionsMixin options) {
        final InputStream is;
        if (url != null) {
            try {
                is = url.openStream();
            } catch (Exception e) {
                throw new JikkouException("Can't read specification from URL '" + url + "': "
                        + e.getMessage());
            }
        } else if (file != null) {
            try {
                is = new FileInputStream(file);
            } catch (Exception e) {
                throw new JikkouException("Can't read specification from file '" + file + "': "
                        + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("no specification");
        }

        Map<String, Object> templatingVars = JikkouParams.TEMPLATING_VARS_CONFIG.get(JikkouConfig.get());
        templatingVars.putAll(options.getClientVars());

        return READER.read(is, templatingVars, options.getClientLabels());
    }
}