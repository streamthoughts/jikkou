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
package io.streamthoughts.jikkou.extension.aiven.change;

import io.streamthoughts.jikkou.api.change.ValueChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaQuotaAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KafkaQuotaChangeComputer extends ValueChangeComputer<V1KafkaQuota, KafkaQuotaEntry> {


    /**
     * Creates a new {@link KafkaQuotaChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaQuotaChangeComputer(boolean deleteOrphans) {
        super(new KeyMapper(), new ValueMapper(), deleteOrphans);
    }

    static class KeyMapper implements ChangeKeyMapper<V1KafkaQuota> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull Object apply(@NotNull V1KafkaQuota o) {
            return List.of(o.getSpec().getUser(), o.getSpec().getClientId());
        }
    }

    static class ValueMapper implements ChangeValueMapper<V1KafkaQuota, KafkaQuotaEntry> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull KafkaQuotaEntry apply(@Nullable V1KafkaQuota before,
                                              @Nullable V1KafkaQuota after) {
            if (after != null)
                return KafkaQuotaAdapter.map(after);
            if (before != null) {
                return KafkaQuotaAdapter.map(before);
            }
            throw new IllegalArgumentException("both arguments are null");
        }
    }
}
