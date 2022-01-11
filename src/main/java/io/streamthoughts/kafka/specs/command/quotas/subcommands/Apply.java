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
package io.streamthoughts.kafka.specs.command.quotas.subcommands;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.QuotaChange;
import io.streamthoughts.kafka.specs.change.QuotaChangeOptions;
import io.streamthoughts.kafka.specs.command.quotas.QuotasCommand;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.manager.KafkaResourceManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "apply",
         description = "Apply all changes to the Kafka client quotas."
)
public class Apply extends QuotasCommand.Base {

    @CommandLine.Option(
            names = "--delete-config-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteConfigOrphans;

    @CommandLine.Option(
            names = "--delete-quota-orphans",
            defaultValue = "false",
            description = "Delete client-quotas which exist on the cluster but absent from the specification files"
    )
    Boolean deleteQuotaOrphans;

    public static DescriptionProvider<QuotaChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Unchanged client-quotas %s %s",
                resource.getType(),
                resource.getType().toPettyString(resource.getEntity())
        );
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaResourceManager.UpdateMode getUpdateMode() {
        return KafkaResourceManager.UpdateMode.APPLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuotaChangeOptions getChangeOptions() {
        return new QuotaChangeOptions()
            .withDeleteConfigOrphans(deleteConfigOrphans)
            .withDeleteQuotaOrphans(deleteQuotaOrphans);
    }
}
