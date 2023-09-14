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
package io.streamthoughts.jikkou.extension.aiven.change.handler;

import io.streamthoughts.jikkou.api.change.ChangeDescription;
import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class CreateSchemaRegistryAclEntryChangeHandler extends AbstractChangeHandler<SchemaRegistryAclEntry> {

    /**
     * Creates a new {@link CreateSchemaRegistryAclEntryChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public CreateSchemaRegistryAclEntryChangeHandler(@NotNull final AivenApiClient api) {
        super(api, ChangeType.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<ValueChange<SchemaRegistryAclEntry>>> apply(
            @NotNull List<HasMetadataChange<ValueChange<SchemaRegistryAclEntry>>> changes) {
        return changes.stream()
                .map(it -> executeAsync(it, () -> api.addSchemaRegistryAclEntry(it.getChange().getAfter())))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeDescription getDescriptionFor(@NotNull HasMetadataChange<ValueChange<SchemaRegistryAclEntry>> item) {
        ValueChange<SchemaRegistryAclEntry> change = item.getChange();
        return KafkaChangeDescriptions.of(change.getChangeType(), change.getAfter());
    }
}
