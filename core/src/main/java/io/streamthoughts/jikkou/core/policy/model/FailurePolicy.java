/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy.model;

public enum FailurePolicy {
    /**
     * The current operation is aborted and an error is returned.
     */
    FAIL,
    /**
     * The validation policy is ignored and the current operation is allowed to continue.
     */
    CONTINUE,
    /**
     * The resource is filtered out.
     */
    FILTER
}