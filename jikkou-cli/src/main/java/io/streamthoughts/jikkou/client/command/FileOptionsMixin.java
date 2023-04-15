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

import io.streamthoughts.jikkou.api.io.ResourceLoaderInputs;
import io.streamthoughts.jikkou.api.model.NamedValue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import picocli.CommandLine.Option;

public class FileOptionsMixin implements ResourceLoaderInputs {

    @Option(
            names = {"--files", "-f"},
            arity = "1..*",
            required = true,
            description = "Specify the locations containing the definitions for resources in a YAML file, a directory or a URL (can specify multiple)."
    )
    public List<String> resourceFiles;

    @Option(
            names = {"--file-name", "-n"},
            defaultValue = "**/*.{yaml,yml}",
            description =
                "Specify the pattern used to match YAML file paths when one or multiple directories are given through the files argument. " +
                "Pattern should be passed in the form of 'syntax:pattern'. The \"glob\" and \"regex\" syntaxes are supported (e.g.: **/*.{yaml,yml}). " +
                "If no syntax is specified the 'glob' syntax is used."
    )
    public String pattern;

    @Option(
            names = {"--values-files"},
            arity = "1..*",
            description = "Specify the values-files containing the variables to pass into the template engine built-in object 'Values' (can specify multiple)."
    )
    public List<String> valuesFiles = new LinkedList<>();


    @Option(names = { "--set-label", "-l" },
            description = "Set labels on the command line (can specify multiple values: -s key1=val1 -s key2=val2)"
    )
    public Map<String, Object> clientLabels = new HashMap<>();


    @Option(names = { "--set-value", "-v" },
            description = "Set variables on the command line to pass into the template engine built-in object 'Values' (can specify multiple values: -v key1=val1 -v key2=val2)"
    )
    public Map<String, Object> clientValues = new HashMap<>();

    /** {@inheritDoc} **/
    @Override
    public List<String> getResourceFileLocations() {
        return resourceFiles;
    }

    /** {@inheritDoc} **/
    @Override
    public String getResourceFilePattern() {
        return pattern;
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getValuesFileLocations() {
        return valuesFiles;
    }

    /** {@inheritDoc} **/
    @Override
    public Iterable<NamedValue> getLabels() {
        return NamedValue.setOf(clientLabels);
    }

    /** {@inheritDoc} **/
    @Override
    public Iterable<NamedValue> getValues() {
        return NamedValue.setOf(clientValues);
    }
}