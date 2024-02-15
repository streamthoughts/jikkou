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

@Command(name = "create",
        header = "Create resources from the resource definition files (only non-existing resources will be created).",
        description = "Reconcile the target platform by creating all non-existing resources that are described by the resource definition files passed as arguments."
)
@Singleton
public class CreateResourceCommand extends BaseResourceCommand {

    /** {@inheritDoc} **/
    @Override
    protected @NotNull ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.CREATE;
    }
}
