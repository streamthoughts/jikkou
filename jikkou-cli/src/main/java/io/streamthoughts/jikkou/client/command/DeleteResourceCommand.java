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
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.ReconciliationMode;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;

@Command(name = "delete",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Delete resources that are no longer described by the resource definition files.",
        description = "Reconcile the target platform by deleting all existing resources that are no longer described by the resource definition files passed as arguments.",
        mixinStandardHelpOptions = true
)
@Singleton
public class DeleteResourceCommand extends BaseResourceCommand {

    /** {@inheritDoc} **/
    @Override
    protected @NotNull ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.DELETE;
    }
}
