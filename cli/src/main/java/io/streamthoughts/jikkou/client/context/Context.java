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
package io.streamthoughts.jikkou.client.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.Map;

/**
 * Represent a context configuration.
 *
 * @param configFile       a configuration file in HOCON format.
 * @param configProps      additional config properties.
 */
@ReflectiveAccess
@JsonInclude(JsonInclude.Include.ALWAYS)
public record Context(@JsonProperty("configFile") String configFile,
                      @JsonProperty("configProps") Map<String, Object> configProps) {

    @ConstructorProperties({
            "configFile",
            "configProps"
    })
    public Context {}


    /**
     * Static method to create and return a default context.
     * @return  the context.
     */
    public static Context defaultContext() {
        return new Context(null, Collections.emptyMap());
    }

    /**
     * Loads the configuration for this context.
     * Configuration is reloaded each time this method is invoked.
     *
     * @return  the configuration.
     */
    public synchronized JikkouConfig load() {
        return configFile() != null ? JikkouConfig.load(configProps(), configFile()) : JikkouConfig.load(configProps());
    }
}
