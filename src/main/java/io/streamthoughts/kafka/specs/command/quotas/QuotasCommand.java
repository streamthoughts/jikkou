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
package io.streamthoughts.kafka.specs.command.quotas;

import io.streamthoughts.kafka.specs.change.*;
import io.streamthoughts.kafka.specs.command.WithAdminClientCommand;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.quotas.subcommands.*;
import io.streamthoughts.kafka.specs.command.quotas.subcommands.internal.DescribeQuotas;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.operation.acls.AclOperation;
import io.streamthoughts.kafka.specs.operation.quotas.QuotaOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@CommandLine.Command(name = "quotas",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Apply the quotas changes described by your specs-file against the Kafka cluster you are currently pointing at.",
        description = "This command can be used to create, alter, delete or describe quotas on a remote Kafka cluster",
        subcommands = {
                Alter.class,
                Apply.class,
                Create.class,
                Delete.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class QuotasCommand extends WithAdminClientCommand {

    public static abstract class Base extends WithSpecificationCommand<QuotaChange> {

        /**
         * Gets the operation to execute.
         *
         * @param client the {@link AdminClient}.
         * @return a new {@link AclOperation}.
         */
        public abstract QuotaOperation getOperation(@NotNull final AdminClient client);

        public abstract QuotaChangeOptions getOptions();

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<ChangeResult<QuotaChange>> executeCommand(final AdminClient client) {

            // Get the list of quotas, that are candidates for this execution, from the SpecsFile.
            final List<V1QuotaObject> expectedStates = loadSpecsObject().quotas();

            // Get the list of quotas, that are candidates for this execution, from the remote Kafka cluster
            final Collection<V1QuotaObject> actualStates = new DescribeQuotas(client).describe();

            // Compute state changes
            Supplier<List<QuotaChange>> supplier = () -> new QuotaChangeComputer().
                    computeChanges(actualStates, expectedStates, getOptions());

            return ChangeExecutor.ofSupplier(supplier).execute(getOperation(client), isDryRun());
        }
    }
}
