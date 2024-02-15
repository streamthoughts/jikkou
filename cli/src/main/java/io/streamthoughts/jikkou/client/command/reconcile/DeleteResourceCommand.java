/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.reconcile;

import io.streamthoughts.jikkou.core.ReconciliationMode;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;

@Command(name = "delete",
        header = "Delete resources that are no longer described by the resource definition files.",
        description = "Reconcile the target platform by deleting all existing resources that are no longer described by the resource definition files passed as arguments."
)
@Singleton
public class DeleteResourceCommand extends BaseResourceCommand {

    /** {@inheritDoc} **/
    @Override
    protected @NotNull ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.DELETE;
    }
}
