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
package io.streamthoughts.jikkou.cli.command.validate;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.model.MetaObject;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.processor.V1SpecFileProcessor;
import io.streamthoughts.jikkou.io.SpecFileLoader;
import io.streamthoughts.jikkou.io.YAMLSpecWriter;
import io.streamthoughts.jikkou.cli.command.SetOptionsMixin;
import io.streamthoughts.jikkou.cli.command.SpecFileOptionsMixin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "validate",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Validate your specification file.",
        description = "This command can be used to validate a Kafka Specs file.",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Callable<Integer> {

    @Mixin
    SpecFileOptionsMixin specOptions;

    @Mixin
    SetOptionsMixin setOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        List<V1SpecFile> files = SpecFileLoader.newForYaml()
                .withPattern(specOptions.pattern)
                .withLabels(setOptions.clientLabels)
                .withVars(setOptions.clientVars)
                .load(specOptions.files);

        V1SpecFileProcessor processor = new V1SpecFileProcessor(JikkouConfig.get());
        for (V1SpecFile file : files) {
            V1SpecFile validate = processor.apply(file);
            MetaObject metaObject = MetaObject.defaults()
                    .setAnnotations(validate.metadata().getAnnotations())
                    .setLabels(validate.metadata().getLabels());
            V1SpecFile validated = new V1SpecFile(metaObject, validate.spec());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            YAMLSpecWriter.instance().write(validated, baos);
            System.out.println(baos);
        }

        return CommandLine.ExitCode.OK;
    }

}
