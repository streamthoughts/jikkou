/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.cli.command;

import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionFactory;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.api.processor.DefaultProcessor;
import io.streamthoughts.jikkou.api.processor.DefaultProcessorFactory;
import io.streamthoughts.jikkou.cli.CLIUtils;
import io.streamthoughts.jikkou.cli.Printer;
import io.streamthoughts.jikkou.io.SpecFileLoader;
import io.vavr.Lazy;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WithSpecificationCommand<T extends Change<?>> extends BaseCommand {

    @Mixin
    SpecFileOptionsMixin specOptions;

    @Spec
    private CommandSpec spec;

    @Mixin
    SetOptionsMixin options;

    private final Lazy<List<V1SpecObject>> object = Lazy.of(() -> {

        List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(JikkouConfig.get())
                .getOrElse(Collections.emptyList());

        var factory = new ReflectiveExtensionFactory()
                .addRootApiPackage()
                .addExtensionPaths(extensionPaths);

        DefaultProcessor processor = new DefaultProcessorFactory(factory)
                .create(JikkouConfig.get());

        List<V1SpecFile> specFiles = SpecFileLoader.newForYaml()
                .withPattern(specOptions.pattern)
                .withLabels(options.clientLabels)
                .withValues(options.clientVars)
                .withValuesFiles(options.values)
                .load(specOptions.files);
        return specFiles.stream().map(processor::apply).map(V1SpecFile::spec).collect(Collectors.toList());
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        if (!execOptions.yes && !isDryRun()) {
            CLIUtils.askToProceed(spec);
        }
        final Collection<ChangeResult<T>> results = execute(object.get());
        Printer.print(results, execOptions.verbose, isDryRun());
        return CommandLine.ExitCode.OK;
    }

    public abstract Collection<ChangeResult<T>> execute(List<V1SpecObject> objects);

    public boolean isDryRun() {
        return execOptions.dryRun;
    }

}
