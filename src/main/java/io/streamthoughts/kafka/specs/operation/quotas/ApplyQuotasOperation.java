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
package io.streamthoughts.kafka.specs.operation.quotas;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.QuotaChange;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

/**
 * Operation to create client quotas.
 */
public class ApplyQuotasOperation extends AbstractQuotaOperation {

    public static DescriptionProvider<QuotaChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Unchanged client-quotas %s %s",
                resource.getType(),
                resource.getType().toPettyString(resource.getEntity())
        );
    });

    public final CreateQuotasOperation create;
    public final AlterQuotasOperation alter;
    public final DeleteQuotasOperation delete;

    public ApplyQuotasOperation(@NotNull final AdminClient client) {
        super(client);
        this.create = new CreateQuotasOperation(client);
        this.alter = new AlterQuotasOperation(client);
        this.delete = new DeleteQuotasOperation(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(final @NotNull QuotaChange change) {
        return switch (change.getOperation()) {
            case ADD -> create.getDescriptionFor(change);
            case UPDATE -> alter.getDescriptionFor(change);
            case DELETE -> delete.getDescriptionFor(change);
            case NONE -> DESCRIPTION.getForResource(change);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(@NotNull final QuotaChange change) {
        return delete.test(change) || create.test(change) || alter.test(change);
    }
}
