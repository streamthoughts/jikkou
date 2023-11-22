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
