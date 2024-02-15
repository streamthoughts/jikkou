/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * The operation applied to a resource state.
 */
@Reflectable
public enum Operation {

    /**
     * An operation that resulted in an existing resource or data being unchanged in the system.
     */
    NONE,
    /**
     * An operation that resulted in a new resource or data being created in the system.
     */
    CREATE,
    /**
     * An operation that resulted in an existing resource or data being deleted in the system.
     */
    DELETE,
    /**
     * An operation that resulted in an existing resource or data being updated in the system.
     */
    UPDATE;

    Operation() {
    }

    public String humanize() {
        var str = this.equals(Operation.NONE) ? "unchanged" : this.name().toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
