/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.repository.LocalResourceOptions;
import io.streamthoughts.jikkou.core.repository.LocalResourceRepository;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import picocli.CommandLine.Option;

public class FileOptionsMixin implements LocalResourceOptions {

    @Option(
            names = {"--files", "-f"},
            arity = "1..*",
            description = LocalResourceRepository.Config.FILE_DESCRIPTION
    )
    public List<String> resourceFiles;

    @Option(
            names = {"--file-name", "-n"},
            defaultValue = "**/*.{yaml,yml}",
            description = LocalResourceRepository.Config.FILE_NAME_DESCRIPTION
    )
    public String resourceFilePattern;

    @Option(
            names = {"--values-files"},
            arity = "1..*",
            description = LocalResourceRepository.Config.VALUES_FILES_DESCRIPTION
    )
    public List<String> valuesFiles = new LinkedList<>();

    @Option(
        names = {"--values-file-name"},
        defaultValue = "**/*.{yaml,yml}",
        description = LocalResourceRepository.Config.VALUES_FILE_NAME_DESCRIPTION
    )
    public String valuesFilePattern;

    @Option(names = { "--set-label", "-l" },
            description = LocalResourceRepository.Config.LABELS_DESCRIPTION
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
        return resourceFilePattern;
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getValuesFileLocations() {
        return valuesFiles;
    }

    @Override
    public String getValuesFilePattern() {
        return valuesFilePattern;
    }

    /** {@inheritDoc} **/
    @Override
    public NamedValueSet getLabels() {
        return NamedValueSet.setOf(clientLabels);
    }

    public NamedValueSet getAnnotations() {
        return NamedValueSet.setOf(clientAnnotations);
    }

    /** {@inheritDoc} **/
    @Override
    public NamedValueSet getValues() {
        return NamedValueSet.setOf(clientValues);
    }
}