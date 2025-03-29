/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;

/**
 * Service interface for loading
 */
public interface ResourceRepository extends Extension {

    /**
     * Gets all the resources for this repository.
     *
     * @return  The list of resources.
     */
    List<? extends HasMetadata> all();
}
