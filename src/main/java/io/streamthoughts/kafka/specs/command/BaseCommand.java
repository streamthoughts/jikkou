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
package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.KafkaSpecs;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.internal.AdminClientUtils;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParentCommand;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

@Command(synopsisHeading      = "%nUsage:%n%n",
         descriptionHeading   = "%nDescription:%n%n",
         parameterListHeading = "%nParameters:%n%n",
         optionListHeading    = "%nOptions:%n%n",
         commandListHeading   = "%nCommands:%n%n",
         mixinStandardHelpOptions = true)
public abstract class BaseCommand implements Callable<Integer> {

    @Mixin
    ExecOptionsMixin execOptions;

    @ParentCommand
    WithAdminClientCommand command;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        return command.withAdminClient(this::call);
    }

    public abstract Integer call(final AdminClient client);

    public final boolean isResourceCandidate(final String resourceName) {
        return includePredicate().test(resourceName) && !excludePredicate().test(resourceName);
    }

    private Predicate<String> includePredicate() {
        return s -> Optional.ofNullable(execOptions.include)
                .map(include -> stream(include).anyMatch(m -> m.matcher(s).matches()))
                .orElse(true);
    }

    private Predicate<String> excludePredicate() {
        return s -> Optional.ofNullable(execOptions.exclude)
                .map(exclude -> stream(exclude).anyMatch(m -> m.matcher(s).matches()))
                .orElse(false);
    }
}
