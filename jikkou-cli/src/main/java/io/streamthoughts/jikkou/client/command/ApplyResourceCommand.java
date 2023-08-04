/*
 * Copyright 2022 The original authors
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

import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "apply",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Update the resources as described by the resource definition files.",
        description = "Reconciles the target platform so that the resources match the resource definition files passed as arguments.",
        mixinStandardHelpOptions = true)
@Singleton
public class ApplyResourceCommand extends BaseResourceCommand {

    @Option(
            names = {"--options"},
            description = "Set the options to be used for computing resource reconciliation (can specify multiple values: --options key1=val1 --options key2=val2)"
    )
    Map<String, Object> options = new HashMap<>();

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
