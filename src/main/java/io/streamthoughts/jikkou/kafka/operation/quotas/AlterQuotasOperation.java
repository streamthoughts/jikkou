/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.operation.quotas;

import io.streamthoughts.jikkou.kafka.Description;
import io.streamthoughts.jikkou.kafka.change.Change;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.internal.DescriptionProvider;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

/**
 * Operation to alter client quotas.
 */
public class AlterQuotasOperation extends AbstractQuotaOperation {

    public static DescriptionProvider<QuotaChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Alter quotas %s %s",
                resource.getType(),
                resource.getType().toPettyString(resource.getEntity())
        );
    });

    /**
     * Creates a new {@link AlterQuotasOperation} instance.
     *
     * @param client    the {@link AdminClient}.
     */
    public AlterQuotasOperation(@NotNull final AdminClient client) {
        super(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(@NotNull final QuotaChange change) {
        return DESCRIPTION.getForResource(change);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(@NotNull final QuotaChange change) {
        return change.getOperation() == Change.OperationType.UPDATE;
    }
}
