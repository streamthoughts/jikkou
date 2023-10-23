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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationContext {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationContext.class);
    public static final String EMPTY_CONTEXT = "";
    private final ObjectMapper objectMapper;

    private static final String JIKKOU_CONFIG_ENV = "JIKKOUCONFIG";

    private static final String DEFAULT_CONFIG_FILE = ".jikkou/config";
    private final File configFile;
    
    public ConfigurationContext(ObjectMapper objectMapper) {
        this(getDefaultConfigFile(), objectMapper);
    }

    @NotNull
    private static File getDefaultConfigFile() {
        return Optional.ofNullable(System.getenv(JIKKOU_CONFIG_ENV))
                .map(File::new)
                .orElse(new File(new File(System.getProperty("user.home")), DEFAULT_CONFIG_FILE));
    }

    /**
     * Creates a new {@link ConfigurationContext} instance.
     *
     * @param configFile    the Jikkouconfig file.
     * @param objectMapper  the objectMapper used to read the Jikkouconfig.
     */
    public ConfigurationContext(@NotNull final File configFile, @NotNull final ObjectMapper objectMapper) {
        this.configFile = Objects.requireNonNull(configFile, "configFile cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    public void setContext(String contextName, Context context) {
        if (!isExists()) {
            tryWriteConfiguration(new Configuration(contextName).addConfigurationContext(contextName, context));
            return;
        }

        var configuration = tryReadConfiguration();

        tryWriteConfiguration(configuration.addConfigurationContext(contextName, context));
    }

    public String getConfigFile() {
        return configFile.getAbsolutePath();
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

        String currentContext = configuration.getCurrentContext();
        return Optional.ofNullable(configuration.configurationContexts().get(currentContext))
                .orElseThrow(() -> new JikkouRuntimeException("Empty configuration context for '" + currentContext + "'"));
    }

    public String getCurrentContextName() {
        if (!isExists()) {
            return EMPTY_CONTEXT;
        }

        var configuration = tryReadConfiguration();


        return Optional.ofNullable(configuration.getCurrentContext()).orElse(EMPTY_CONTEXT);
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
            return objectMapper.readValue(configFile, Configuration.class);
        }
        catch (IOException e) {
            throw new JikkouRuntimeException("Couldn't read configuration file ~/" + DEFAULT_CONFIG_FILE + ".", e);
        }
    }

    private void tryWriteConfiguration(Configuration configuration) {
        try {
            if (!isExists() && configFile.getParentFile().mkdirs()) {
                LOG.debug("Creating missing parent directory for: {}", configFile);
            }
            LOG.debug("Writing configuration to {}: {}", configFile, configuration);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configuration);
        }
        catch (IOException e) {
            throw new JikkouRuntimeException(
                    "Couldn't write configuration file " + configFile + ".",
                    e);
        }
    }
}
