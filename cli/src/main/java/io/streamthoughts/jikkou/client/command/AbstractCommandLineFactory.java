/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.micronaut.context.ApplicationContext;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public abstract class AbstractCommandLineFactory {

    public static final String OPTION_PREFIX = "--";
    protected final ApplicationContext applicationContext;

    protected final JikkouApi api;

    public AbstractCommandLineFactory(@NotNull ApplicationContext applicationContext,
                                      @NotNull JikkouApi api) {
        this.applicationContext = applicationContext;
        this.api = api;
    }

    public abstract CommandLine createCommandLine();


    protected CommandLine.Model.OptionSpec createOptionSpec(ApiOptionSpec option,
                                                            AbstractApiCommand command) {
        CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec
                .builder(buildOptionName(option))
                .type(option.typeClass())
                .description(option.description())
                .required(option.required())
                .defaultValue(option.defaultValue())
                .required(option.required())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        return command.option(option, value);
                    }
                });
        return builder.build();
    }

    @NotNull
    private static String buildOptionName(ApiOptionSpec option) {
        return OPTION_PREFIX + normalizeOptionName(option);
    }

    @NotNull
    private static String normalizeOptionName(ApiOptionSpec option) {
        return option.name().replaceAll("\\.", "-");
    }
}
