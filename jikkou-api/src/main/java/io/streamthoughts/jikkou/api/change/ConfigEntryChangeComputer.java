/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.api.change;

import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import java.util.Collections;
import java.util.List;

public class ConfigEntryChangeComputer extends AbstractChangeComputer<ConfigValue, ConfigValue, ConfigEntryChange>{
    
    /**
     * Creates a new {@link ConfigEntryChangeComputer} instance.
     */
    public ConfigEntryChangeComputer() {
        this(true);
    }

    /**
     * Creates a new {@link ConfigEntryChangeComputer} instance.
     *
     * @param isDeleteOrphansEnabled {@code true} to delete orphaned config entries.
     */
    public ConfigEntryChangeComputer(boolean isDeleteOrphansEnabled) {
        super(ConfigValue::getName, new IdentityChangeValueMapper<>(), isDeleteOrphansEnabled);
    }

    /** {@inheritDoc} **/
    @Override
    protected ObjectMeta getObjectMetadata(ConfigValue before, ConfigValue after) {
        return new ObjectMeta();
    }

    /** {@inheritDoc} **/
    @Override
    protected ChangeType getChangeType(ConfigValue before, ConfigValue after) {
        return before == null ? ChangeType.ADD : after == null ? ChangeType.DELETE : ChangeType.UPDATE;
    }

    /** {@inheritDoc} **/
    @Override
    public List<ConfigEntryChange> buildChangeForDeleting(ConfigValue before) {
        if (before.isDeletable())
            return List.of(new ConfigEntryChange(before.getName(), ValueChange.withBeforeValue(before.value())));

        return Collections.emptyList();
    }

    /** {@inheritDoc} **/
    @Override
    public List<ConfigEntryChange> buildChangeForUpdating(ConfigValue before, ConfigValue after) {
        return List.of(new ConfigEntryChange(before.getName(), ValueChange.with(before.value(), after.value())));
    }

    /** {@inheritDoc} **/
    @Override
    public List<ConfigEntryChange> buildChangeForNone(ConfigValue before, ConfigValue after) {
        return List.of(new ConfigEntryChange(before.getName(), ValueChange.with(before.value(), after.value())));
    }

    /** {@inheritDoc} **/
    @Override
    public List<ConfigEntryChange> buildChangeForCreating(ConfigValue after) {
        return List.of(new ConfigEntryChange(after.getName(), ValueChange.withAfterValue(after.value())));
    }
}
