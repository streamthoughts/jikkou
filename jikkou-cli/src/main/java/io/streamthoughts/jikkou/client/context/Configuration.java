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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.ReflectiveAccess;
import java.util.LinkedHashMap;
import java.util.Map;

@ReflectiveAccess
@JsonClassDescription
public class Configuration {

    @JsonProperty("currentContext")
    private String currentContext;

    @JsonAnyGetter
    private final Map<String, Context> configurationContexts = new LinkedHashMap<>();

    @JsonCreator
    public Configuration(String currentContext) {
        this.currentContext = currentContext;
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String currentContext) {
        this.currentContext = currentContext;
    }

    @JsonAnyGetter
    public Map<String, Context> configurationContexts() {
        return configurationContexts;
    }

    @JsonAnySetter
    public Configuration addConfigurationContext(String contextName, Context configuration) {
        configurationContexts.put(contextName, configuration);
        return this;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "currentContext='" + currentContext + '\'' +
                ", configurationContexts=" + configurationContexts +
                '}';
    }
}