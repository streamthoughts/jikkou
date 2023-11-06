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
package io.streamthoughts.jikkou.core.config;

import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigurationSupport<C extends ConfigurationSupport<C>> implements Configurable {

    private Configuration configuration;

    /**
     * Creates a new {@link ConfigurationSupport} instance.
     */
    public ConfigurationSupport() {}

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        this.configuration = Objects.requireNonNull(configuration, "configuration cannot be null");
    }

    protected <T> T get(ConfigProperty<T> property) {
        return property.evaluate(configuration);
    }

    protected C with(final ConfigProperty<String> property, final String value) {
        return newInstance(configuration.edit()
                .with(property.key(), value)
                .build()
        );
    }

    protected C with(final ConfigProperty<Boolean> property, final boolean value) {
        return newInstance(configuration.edit()
                .with(property.key(), value)
                .build()
        );
    }

    protected C with(final ConfigProperty<Long> property, final long value) {
        return newInstance(configuration.edit()
                .with(property.key(), value)
                .build()
        );
    }

    protected C with(final ConfigProperty<Integer> property, final int value) {
        return newInstance(configuration.edit()
                .with(property.key(), value)
                .build()
        );
    }

    /**
     * Creates a new instance for the given configuration.
     *
     * @param configuration the {@link Configuration} to use.
     * @return  new instance of {@link C}.
     */
    protected abstract C newInstance(final Configuration configuration);

    protected abstract Set<ConfigProperty<?>> defaultConfigProperties();

    public Configuration asConfiguration() {
        Map<String, Object> config = new HashMap<>();
        for (ConfigProperty<?> property : defaultConfigProperties())  {
            Object defaultValue = property.defaultValue();
            if (defaultValue != null) {
                config.put(property.key(), defaultValue);
            }
        }
        config.putAll(configuration.asMap());
        return Configuration.from(config);
    }
}
