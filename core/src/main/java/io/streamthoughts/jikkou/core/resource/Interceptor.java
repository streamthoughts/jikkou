/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;

/**
 * This interface is used to define extension class that can intercept resources.
 *
 * @see Validation
 * @see Transformation
 * @see io.streamthoughts.jikkou.core.converter.Converter
 */
public interface Interceptor extends
        HasMetadataAcceptable,
        HasPriority,
        Extension {
}
