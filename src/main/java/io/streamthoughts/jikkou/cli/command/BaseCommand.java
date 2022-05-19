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

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.filter.DefaultKafkaResourceFilter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.util.Optional;
import java.util.concurrent.Callable;

@Command(synopsisHeading      = "%nUsage:%n%n",
         descriptionHeading   = "%nDescription:%n%n",
         parameterListHeading = "%nParameters:%n%n",
         optionListHeading    = "%nOptions:%n%n",
         commandListHeading   = "%nCommands:%n%n",
         mixinStandardHelpOptions = true)
public abstract class BaseCommand implements Callable<Integer> {

    @Mixin
    ExecOptionsMixin execOptions;

    public final boolean isResourceCandidate(final String resourceName) {
        return new DefaultKafkaResourceFilter()
                .withExcludes(Optional.ofNullable(execOptions.exclude)
                        .or(() -> JikkouParams.EXCLUDE_RESOURCES.getOption(JikkouConfig.get()).toJavaOptional()).orElse(null))
                .withIncludes(Optional.ofNullable(execOptions.include)
                        .or(() -> JikkouParams.INCLUDE_RESOURCES.getOption(JikkouConfig.get()).toJavaOptional()).orElse(null))
                .test(resourceName);
    }
}
