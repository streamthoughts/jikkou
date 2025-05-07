/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.aws.model;

/**
 * Schema compatibility levels.
 *
 * @see software.amazon.awssdk.services.glue.model.Compatibility
 */
public enum Compatibility {

    NONE,
    DISABLED,
    BACKWARD,
    BACKWARD_ALL,
    FORWARD,
    FORWARD_ALL,
    FULL,
    FULL_ALL;
}
