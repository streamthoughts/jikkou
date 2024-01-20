/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
