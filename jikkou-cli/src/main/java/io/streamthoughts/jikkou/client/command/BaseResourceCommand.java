/*
 * Copyright 2021 The original authors
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

import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.io.YAMLResourceLoader;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.client.Jikkou;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(synopsisHeading      = "%nUsage:%n%n",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        mixinStandardHelpOptions = true)
public abstract class BaseResourceCommand implements Callable<Integer> {

    @Mixin
    ExecOptionsMixin execOptions;

    @Mixin
    FileOptionsMixin fileOptions;

    @Mixin
    SelectorOptionsMixin selectorOptions;

    @Inject
    JikkouApi api;

    @Inject
    YAMLResourceLoader loader;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {

        HasItems resources = loadResources();

        final List<ChangeResult<Change>> results = api.apply(
                resources,
                getReconciliationMode(),
                ReconciliationContext.with(
                        selectorOptions.getResourceSelectors(),
                        getReconciliationConfiguration(),
                        isDryRun()
                ));

        return execOptions.format.print(results, isDryRun(), Jikkou.getExecutionTime());
    }

    protected @NotNull HasItems loadResources() {
        return loader.load(fileOptions);
    }

    protected abstract Configuration getReconciliationConfiguration();

    protected abstract ReconciliationMode getReconciliationMode();

    public boolean isDryRun() {
        return execOptions.dryRun;
    }
}
