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
package io.streamthoughts.jikkou.kafka.control.operation.topics;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.vavr.concurrent.Future;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

public class ApplyTopicOperation implements TopicOperation{

    private final CreateTopicOperation create;
    private final AlterTopicOperation alter;
    private final DeleteTopicOperation delete;

    /**
     * Creates a new {@link ApplyTopicOperation} instance.
     *
     * @param client    the {@link AdminClient}.
     */
    public ApplyTopicOperation(final @NotNull AdminClient client) {
        this.create = new CreateTopicOperation(client);
        this.alter = new AlterTopicOperation(client);
        this.delete = new DeleteTopicOperation(client);
    }

    /** {@inheritDoc} */
    @Override
    public ChangeDescription getDescriptionFor(final @NotNull TopicChange change) {
        return switch (change.getChange()) {
            case ADD -> create.getDescriptionFor(change);
            case UPDATE -> alter.getDescriptionFor(change);
            case DELETE -> delete.getDescriptionFor(change);
            case NONE ->  new TopicChangeDescription(change);
        };
    }

    /** {@inheritDoc} */
    @Override
    public boolean test(final TopicChange change) {
        return delete.test(change) || create.test(change) || alter.test(change);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Map<String, List<Future<Void>>> doApply(final @NotNull Collection<TopicChange> changes) {
        HashMap<String, List<Future<Void>>> results = new HashMap<>();
        results.putAll(delete.apply(changes));
        results.putAll(create.apply(changes));
        results.putAll(alter.apply(changes));
        return results;
    }
}
