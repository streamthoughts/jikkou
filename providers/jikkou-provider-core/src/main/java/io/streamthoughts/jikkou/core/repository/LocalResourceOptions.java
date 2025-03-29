/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.util.List;

/**
 * Inputs for loading resources.
 */
public interface LocalResourceOptions {

    /**
     * Get the locations of the resource definition files to load.
     *
     * @return the location paths.
     */
    List<String> getResourceFileLocations();

    /**
     * Get the pattern to use for matching the resource definition files to be loaded.
     *
     * @return the string pattern.
     */
    String getResourceFilePattern();

    /**
     * Get the locations of the values files to load.
     *
     * @return the location paths.
     */
    List<String> getValuesFileLocations();

    /**
     * Get the pattern to use for matching the values files to be loaded.
     *
     * @return the string pattern.
     */
    String getValuesFilePattern();

    /**
     * Get the additional labels to be used when templating is enabled.
     *
     * @return the labels
     */
    NamedValueSet getLabels();

    /**
     * Get the additional values to be used when templating is enabled.
     *
     * @return the values.
     */
    NamedValueSet getValues();
}
