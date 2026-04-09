/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.change;

import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.extension.confluent.api.data.RoleBindingData;

public final class RoleBindingChangeDescription {

    public static TextDescription of(Operation type, RoleBindingData entry) {
        return () -> String.format("%s Confluent Cloud Role Binding for principal '%s' (role=%s, crnPattern=%s)",
            type.humanize(),
            entry.principal(),
            entry.roleName(),
            entry.crnPattern()
        );
    }
}
