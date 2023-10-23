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

import io.streamthoughts.jikkou.core.change.ValueChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KafkaAclEntryChangeComputer extends ValueChangeComputer<V1KafkaTopicAclEntry, KafkaAclEntry> {


    /**
     * Creates a new {@link KafkaAclEntryChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaAclEntryChangeComputer(boolean deleteOrphans) {
        super(new KeyMapper(), new ValueMapper(), deleteOrphans);
    }

    record Key(String username, String topic, Permission permission) {}

    static class KeyMapper implements ChangeKeyMapper<V1KafkaTopicAclEntry> {
        /** {@inheritDoc} **/
        @Override
        public @NotNull Object apply(@NotNull V1KafkaTopicAclEntry o) {
            return new Key(o.getSpec().getUsername(), o.getSpec().getTopic(), o.getSpec().getPermission());
        }
    }


    static class ValueMapper implements ChangeValueMapper<V1KafkaTopicAclEntry, KafkaAclEntry> {
        /** {@inheritDoc} **/
        @Override
        public @NotNull KafkaAclEntry apply(@Nullable V1KafkaTopicAclEntry before,
                                            @Nullable V1KafkaTopicAclEntry after) {
            if (after != null)
                return KafkaAclEntryAdapter.map(after);
            if (before != null) {
                return KafkaAclEntryAdapter.map(before);
            }
            throw new IllegalArgumentException("both arguments are null");
        }
    }
}
