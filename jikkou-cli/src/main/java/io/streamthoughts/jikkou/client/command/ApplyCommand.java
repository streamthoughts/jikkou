/*
 * Copyright 2022 StreamThoughts.
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
import io.streamthoughts.jikkou.api.model.ResourceList;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "apply",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Apply configurations to resources by files",
        description = "This command can be used to create, alter, or delete resources",
        mixinStandardHelpOptions = true)
public class ApplyCommand extends BaseResourceCommand {

    @Option(
            names = {"--set-option"},
            description = "Set the options to be used for computing resource reconciliation (can specify multiple values: -o key1=val1 -o key2=val2)"
    )
    Map<String, Object> options = new HashMap<>();

    @Option(
            names = {"--kind"},
            defaultValue = "",
            description = "Apply the reconciliation only for the specified kind of resources."
    )
    String kind = "";

    /** {@inheritDoc } **/
    @Override
    protected @NotNull ResourceList loadResources() {
        ResourceList resources = super.loadResources();
        return !kind.isBlank() ? resources.allResourcesForKind(kind) : resources;
    }

    /** {@inheritDoc } **/
    @Override
    protected Configuration getReconciliationConfiguration() {
        return Configuration.from(options);
    }

    /** {@inheritDoc } **/
    @Override
    protected ReconciliationMode getReconciliationMode() {
        return ReconciliationMode.APPLY_ALL;
    }
}
