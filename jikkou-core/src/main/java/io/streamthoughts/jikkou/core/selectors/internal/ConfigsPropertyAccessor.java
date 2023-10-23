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
package io.streamthoughts.jikkou.core.selectors.internal;

import io.streamthoughts.jikkou.core.exceptions.SelectorException;
import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import java.util.Objects;
import java.util.Optional;

public class ConfigsPropertyAccessor implements PropertyAccessor {


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class[]{Configs.class};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRead(final Object target,
                           final String name) throws SelectorException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object read(final Object target,
                       final String name) throws SelectorException {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        ConfigValue configValue = ((Configs) target).get(name);
        return Optional.ofNullable(configValue).map(ConfigValue::value).orElse(null);
    }
}
