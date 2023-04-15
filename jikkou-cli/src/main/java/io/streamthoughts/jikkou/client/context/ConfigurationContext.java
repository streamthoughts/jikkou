/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigurationContext {

    private final ObjectMapper OBJECT_MAPPER = JsonMapper
            .builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .build();

    private static final String CONFIG_FILE = ".jikkou/config";
    private final File configFile;


    public ConfigurationContext() {
        this(new File(System.getProperty("user.home")));
    }

    public ConfigurationContext(File configDirectory) {
        this.configFile = new File(configDirectory, CONFIG_FILE);
    }

    public void setContext(String contextName, Context context) {
        if (!isExists()) {
            tryWriteConfiguration(new Configuration(contextName).addConfigurationContext(contextName, context));

            return;
        }

        var configuration = tryReadConfiguration();

        tryWriteConfiguration(configuration.addConfigurationContext(contextName, context));
    }

    public Context getContext(String contextName) {
        if (!isExists()) {
            return Context.defaultContext();
        }

        var configuration = tryReadConfiguration();

        return configuration.configurationContexts().get(contextName);
    }

    public boolean isExists() {
        return configFile.exists();
    }

    public Map<String, Context> getContexts() {
        Map<String, Context> contexts = new LinkedHashMap<>();
        if (!isExists()) {
            return contexts;
        }

        var configuration = tryReadConfiguration();
        return configuration.configurationContexts();
    }

    public Context getCurrentContext() {
        if (!isExists()) {
            return Context.defaultContext();
        }

        var configuration = tryReadConfiguration();

        return configuration.configurationContexts().get(configuration.getCurrentContext());
    }

    public String getCurrentContextName() {
        if (!isExists()) {
            return "";
        }

        var configuration = tryReadConfiguration();

        return configuration.getCurrentContext();
    }

    public boolean setCurrentContext(String contextName) {
        if (!isExists()) {
            return false;
        }

        var configuration = tryReadConfiguration();

        if (!configuration.configurationContexts().containsKey(contextName)) {
            return false;
        }

        configuration.setCurrentContext(contextName);

        tryWriteConfiguration(configuration);

        return true;
    }

    private Configuration tryReadConfiguration() {
        try {
            return OBJECT_MAPPER.readValue(configFile, Configuration.class);
        }
        catch (IOException e) {
            throw new JikkouRuntimeException("Couldn't read configuration file ~/" + CONFIG_FILE + ".", e);
        }
    }

    private void tryWriteConfiguration(Configuration configuration) {
        try {
            OBJECT_MAPPER.writeValue(configFile, configuration);
        }
        catch (IOException e) {
            throw new JikkouRuntimeException("Couldn't write configuration file " + configFile + ".",
                    e);
        }
    }
}
