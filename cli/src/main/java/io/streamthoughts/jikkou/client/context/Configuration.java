/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.context;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.LinkedHashMap;
import java.util.Map;

@Reflectable
public class Configuration {

    @JsonProperty("currentContext")
    private String currentContext;

    @JsonAnyGetter
    private final Map<String, Context> configurationContexts = new LinkedHashMap<>();

    @ConstructorProperties({
            "currentContext"
    })
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