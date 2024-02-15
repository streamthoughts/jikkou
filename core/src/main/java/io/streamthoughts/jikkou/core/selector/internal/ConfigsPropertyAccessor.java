/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector.internal;

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
