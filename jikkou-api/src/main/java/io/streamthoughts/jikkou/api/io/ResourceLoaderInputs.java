/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.io;

import io.streamthoughts.jikkou.api.model.NamedValue;
import java.util.List;

/**
 * Represents the
 */
public interface ResourceLoaderInputs {

    /**
     * Get the locations of the resource definition files to load.
     *
     * @return the location paths.
     */
    List<String> getResourceFileLocations();

    /**
     * Get the pattern to use for matching the resource definition files to be loaded.
     *
     * @return  the string pattern.
     */
    String getResourceFilePattern();

    /**
     * Get the locations of the values files to load.
     *
     * @return the location paths.
     */
    List<String> getValuesFileLocations();

    /**
     * Get the additional labels to be used when templating is enabled.
     *
     * @return  the labels
     */
    Iterable<NamedValue> getLabels();

    /**
     * Get the additional values to be used when templating is enabled.
     * @return  the values.
     */
    Iterable<NamedValue> getValues();
}
