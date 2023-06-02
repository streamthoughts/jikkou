/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.schema.registry.change.handler;

import static io.streamthoughts.jikkou.api.control.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.control.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.control.ChangeType.UPDATE;

import io.streamthoughts.jikkou.api.control.ChangeResponse;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateSchemaSubjectChangeHandler extends AbstractSchemaSubjectChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateSchemaSubjectChangeHandler.class);

    /**
     * Creates a new {@link UpdateSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public UpdateSchemaSubjectChangeHandler(@NotNull final SchemaRegistryApi api) {
        super(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<SchemaSubjectChange>> apply(@NotNull List<SchemaSubjectChange> changes) {

        List<ChangeResponse<SchemaSubjectChange>> results = new ArrayList<>();
        for (SchemaSubjectChange change : changes) {
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            ValueChange<String> schema = change.getSchema();

            if (UPDATE == schema.getChangeType()) {
                future = future.thenComposeAsync(unused -> registerSubjectSchema(change));
            }

            ValueChange<CompatibilityLevels> compatibilityLevels = change.getCompatibilityLevels();
            if (UPDATE == compatibilityLevels.getChangeType() || ADD == compatibilityLevels.getChangeType()) {
                future = future.thenComposeAsync(unused -> updateCompatibilityLevel(change));
            }

            if (DELETE == compatibilityLevels.getChangeType()) {
                future = future.thenComposeAsync(unused -> deleteCompatibilityLevel(change));
            }
            results.add(new ChangeResponse<>(change, future));
        }
        return results;
    }

    private CompletableFuture<Void> deleteCompatibilityLevel(SchemaSubjectChange change) {
        CompatibilityLevels compatibilityLevels = change.getCompatibilityLevels().getAfter();
        return api
                .updateConfigCompatibility(change.getSubject(), new CompatibilityObject(compatibilityLevels.name()))
                .thenApplyAsync(compatibilityObject -> {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "Deleted compatibility for subject '{}' to '{}'",
                                change.getSubject(),
                                compatibilityObject.compatibility());
                    }
                    return null;
                });
    }
}
