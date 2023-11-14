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

import io.streamthoughts.jikkou.client.command.validate.ValidationErrorsWriter;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.io.ResourceLoaderFacade;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasItems;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "diff",
        header = "Show changes required by the current resource definitions.",
        description = """
               Generates a speculative reconciliation plan, showing the changes Jikkou would apply to reconcile the resource definitions.
               This command does not actually perform the reconciliation actions.
               """)
@Singleton
public class DiffCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Mixin
    FileOptionsMixin fileOptions;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;
    @Mixin
    ConfigOptionsMixin configOptionsMixin;

    // API
    @Inject
    JikkouApi api;
    @Inject
    ResourceLoaderFacade loader;
    @Inject ResourceWriter writer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {

        try {
            ApiResourceChangeList result = api.getDiff(
                    getResources(),
                    getReconciliationContext()
            );
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                writer.write(formatOptions.format, result, baos);
                System.out.println(baos);
                return CommandLine.ExitCode.OK;
            }
        } catch (ValidationException exception) {
            System.out.println(ValidationErrorsWriter.write(exception.errors()));
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    @NotNull
    private HasItems getResources() {
        return loader.load(fileOptions);
    }

    @NotNull
    private  ReconciliationContext getReconciliationContext() {
        return ReconciliationContext
                .builder()
                .dryRun(true)
                .configuration(configOptionsMixin.getConfiguration())
                .selectors(selectorOptions.getResourceSelectors())
                .labels(fileOptions.getLabels())
                .annotations(fileOptions.getAnnotations())
                .build();
    }
}
