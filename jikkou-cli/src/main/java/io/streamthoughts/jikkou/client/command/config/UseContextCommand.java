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
package io.streamthoughts.jikkou.client.command.config;

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
public class UseContextCommand implements Runnable {

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