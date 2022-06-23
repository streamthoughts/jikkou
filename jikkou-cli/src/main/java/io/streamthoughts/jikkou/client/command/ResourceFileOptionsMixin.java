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
package io.streamthoughts.jikkou.client.command;

import java.util.List;
import picocli.CommandLine.Option;

public class ResourceFileOptionsMixin {

    @Option(
            names = {"--files", "-f"},
            arity = "1..*",
            required = true,
            description = "Specify the locations containing the specifications for Kafka resources in a YAML file, a directory or a URL (can specify multiple)."
    )
    public List<String> files;

    @Option(
            names = {"--file-name", "-n"},
            defaultValue = "**/*.{yaml,yml}",
            description =
                "Specify the pattern used to match YAML file paths when one or multiple directories are given through the files argument. " +
                "Pattern should be passed in the form of 'syntax:pattern'. The \"glob\" and \"regex\" syntaxes are supported (e.g.: **/*.{yaml,yml}). " +
                "If no syntax is specified the 'glob' syntax is used."
    )
    public String pattern;
}