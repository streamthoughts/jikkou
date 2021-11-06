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
package io.streamthoughts.kafka.specs.command.validate;

import io.streamthoughts.kafka.specs.SpecFileValidator;
import io.streamthoughts.kafka.specs.YAMLClusterSpecWriter;
import io.streamthoughts.kafka.specs.command.SetOptionsMixin;
import io.streamthoughts.kafka.specs.command.SpecFileOptionsMixin;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.model.MetaObject;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.OutputStream;
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

    @CommandLine.ArgGroup(multiplicity = "1")
    SpecFileOptionsMixin specOptions;

    @CommandLine.Mixin
    SetOptionsMixin setOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        V1SpecFile file = specOptions.parse(setOptions);

        final SpecFileValidator validator = SpecFileValidator.newDefault();
        validator.configure(JikkouConfig.get());

        V1SpecFile validate = validator.apply(file);

        OutputStream os = System.out;
        MetaObject metaObject = MetaObject.defaults()
                .setAnnotations(validate.metadata().getAnnotations())
                .setLabels(validate.metadata().getLabels());
        V1SpecFile validated = new V1SpecFile(metaObject, validate.specs());
        YAMLClusterSpecWriter.instance().write(validated, os);
        return CommandLine.ExitCode.OK;
    }

}
