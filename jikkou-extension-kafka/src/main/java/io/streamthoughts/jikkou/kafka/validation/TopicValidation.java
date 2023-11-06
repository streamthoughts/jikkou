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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaTopic}.
 */
@HandledResource(type = V1KafkaTopic.class)
public abstract class TopicValidation implements Validation<V1KafkaTopic> {

    private Configuration config;

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = config;
    }

    public Configuration config() {
        return Optional.ofNullable(config).orElseThrow(() -> new IllegalStateException("not configured."));
    }
}
