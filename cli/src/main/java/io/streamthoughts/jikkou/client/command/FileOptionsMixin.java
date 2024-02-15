/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.io.ResourceLoaderInputs;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
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
            description = "Set labels on the command line (can specify multiple values)."
    )
    public Map<String, Object> clientLabels = new HashMap<>();

    @Option(names = { "--set-annotation"},
            description = "Set annotations on the command line (can specify multiple)."
    )
    public Map<String, Object> clientAnnotations = new HashMap<>();

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
        return NamedValueSet.setOf(clientLabels);
    }

    public Iterable<NamedValue> getAnnotations() {
        return NamedValueSet.setOf(clientAnnotations);
    }

    /** {@inheritDoc} **/
    @Override
    public Iterable<NamedValue> getValues() {
        return NamedValueSet.setOf(clientValues);
    }
}