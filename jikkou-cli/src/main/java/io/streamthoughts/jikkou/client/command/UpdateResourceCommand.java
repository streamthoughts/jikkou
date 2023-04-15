/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import picocli.CommandLine.Command;

@Command(name = "update",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Create or update resources from the resource definition files",
        description = "Reconcile the target platform by creating or updating resources that are described by the resource definition files passed as arguments.",
        mixinStandardHelpOptions = true
)
public class UpdateResourceCommand extends BaseResourceCommand {

    /** {@inheritDoc} **/
    @Override
    protected Configuration getReconciliationConfiguration() {
        return Configuration.empty();
    }

    /** {@inheritDoc} **/
    @Override
    protected ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.UPDATE;
    }
}
