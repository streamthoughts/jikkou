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
package io.streamthoughts.jikkou.schema.registry.change.handler;

import io.streamthoughts.jikkou.api.control.ChangeResponse;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class CreateSchemaSubjectChangeHandler extends AbstractSchemaSubjectChangeHandler {

    /**
     * Creates a new {@link CreateSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public CreateSchemaSubjectChangeHandler(@NotNull final SchemaRegistryApi api) {
        super(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<SchemaSubjectChange>> apply(@NotNull List<SchemaSubjectChange> changes) {

        List<ChangeResponse<SchemaSubjectChange>> results = new ArrayList<>();
        for (SchemaSubjectChange change : changes) {

            CompletableFuture<Void> future = registerSubjectSchema(change);
            ValueChange<CompatibilityLevels> compatibilityLevels = change.getCompatibilityLevels();
            if (compatibilityLevels != null) {
                future = future.thenComposeAsync(unused -> updateCompatibilityLevel(change));
            }

            results.add(toChangeResponse(change, future));
        }
        return results;
    }
}
