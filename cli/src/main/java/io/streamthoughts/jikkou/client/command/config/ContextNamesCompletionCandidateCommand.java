/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(
        name = "context-name-completions",
        hidden = true
)
@Singleton
public class ContextNamesCompletionCandidateCommand implements Runnable {

    @Inject
    private ConfigurationContext configurationContext;

    /** {@inheritDoc} **/
    @Override
    public void run() {
        System.out.println(String.join(" ", configurationContext.getContexts().keySet()));
    }
}
