/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.user;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;

public final class UserChangeDescription {

    public static TextDescription of(final ResourceChange change) {
        return () -> String.format("%s authentications for user '%s'.'",
            change.getSpec().getOp().humanize(),
            change.getMetadata().getName()
        );
    }
}
