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
package io.streamthoughts.jikkou.extension.aiven.change.quota;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class KafkaQuotaChangeHandler extends AbstractChangeHandler {
    public KafkaQuotaChangeHandler(@NotNull AivenApiClient api, @NotNull Operation operation) {
        super(api, operation);
    }

    public static class Create extends KafkaQuotaChangeHandler {

        public Create(@NotNull AivenApiClient api) {
            super(api, Operation.CREATE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.createKafkaQuota(getEntry(change, KafkaQuotaEntry.class)))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class Delete extends KafkaQuotaChangeHandler {

        public Delete(@NotNull AivenApiClient api) {
            super(api, Operation.DELETE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.deleteKafkaQuota(getEntry(change, KafkaQuotaEntry.class)))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None<ResourceChange> {
        public None() {
            super(change -> KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaQuotaEntry.class)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaQuotaEntry.class));
    }
}
