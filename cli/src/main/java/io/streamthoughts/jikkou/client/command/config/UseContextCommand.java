/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.completion.ContextNameCompletions;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "use-context",
        description = "Configures jikkou to use the specified context"
)
@Singleton
public class UseContextCommand extends CLIBaseCommand implements Runnable {

    @Parameters(index = "0", description = "Context name", completionCandidates = ContextNameCompletions.class)
    String contextName;

    @Inject
    private ConfigurationContext configurationContext;

    /** {@link} **/
    @Override
    public void run() {
        if (configurationContext.getCurrentContextName().equals(contextName)) {
            System.out.println("Already using context " + contextName);
        }
        else {
            boolean success = configurationContext.setCurrentContext(contextName);

            if (success) {
                System.out.println("Using context '" + contextName + "'");
            }
            else {
                System.out.println("Couldn't change context; create a context named " + contextName + " first");
            }
        }
    }
}