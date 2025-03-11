/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import static picocli.CommandLine.Spec.Target.MIXEE;

import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public final class LoggingMixin {

    public static final String LOGGER_LEVEL = "--logger-level";

    @Spec(MIXEE)
    private CommandSpec mixee; // spec of the command where the @Mixin is used

    String loggerLevel;

    /**
     * Sets the specified verbosity on the LoggingMixin of the top-level command.
     *
     * @param loggerLevel the new root logger level.
     */
    @Option(names = {LOGGER_LEVEL},
            description = {
                    "Specify the log level verbosity to be used while running a command.",
                    "Valid level values are: TRACE, DEBUG, INFO, WARN, ERROR.",
                    "For example, `--logger-level=INFO`"
            })
    public void setLevel(String loggerLevel) {
        // Each subcommand that mixes in the LoggingMixin has its own instance
        // of this class, so there may be many LoggingMixin instances.
        // We want to store the 'level' value in a single, central place,
        // so we find the top-level command,
        // and store the 'verbosity level' on our top-level command's LoggingMixin.
        Object root = mixee.root().userObject();
        if (root instanceof Jikkou jikkou) {
            jikkou.loggingMixin.loggerLevel = loggerLevel;
        } else {
            this.loggerLevel = loggerLevel;
        }
    }
}
