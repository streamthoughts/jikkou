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

@Command(name = "apply",
        header = "Update the resources as described by the resource definition files.",
        description = "Reconciles the target platform so that the resources match the resource definition files passed as arguments."
)
@Singleton
public class ApplyResourceCommand extends BaseResourceCommand {

    /** {@inheritDoc } **/
    @Override
    protected @NotNull ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.FULL;
    }

}
