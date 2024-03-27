/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * Schema modes
 */
@Reflectable
public enum Modes {
    IMPORT,
    READONLY,
    READWRITE,
    FORWARD
}
