/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.change.handlers.quotas;

import io.streamthoughts.jikkou.api.change.ChangeDescription;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class QuotaChangeDescription implements ChangeDescription {

    private final HasMetadataChange<QuotaChange> item;

    public QuotaChangeDescription(final @NotNull HasMetadataChange<QuotaChange> item) {
        this.item = Objects.requireNonNull(item, "item must not be null");
    }

    /** {@inheritDoc} **/
    @Override
    public String textual() {
        QuotaChange change = item.getChange();
        return  String.format("%s quotas %s with entity=[%s], constraints=[%s])",
                ChangeDescription.humanize(change.operation()),
                change.getType(),
                change.getType().toPettyString(change.getEntity()),
                change.getConfigEntryChanges().stream().map(s -> s.name() + "=" + s.valueChange().getAfter()).collect( Collectors.joining( "," ) )
        );
    }
}
