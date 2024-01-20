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
package io.streamthoughts.jikkou.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

/**
 * Utility class to set the root logger level.
 */
public final class Logging {

    private static final String ENV_VAR_JIKKOU_CLI_LOG_LEVEL = "JIKKOU_CLI_LOG_LEVEL";

    public static void configureRootLoggerLevel() {
        Optional.ofNullable(System.getenv(ENV_VAR_JIKKOU_CLI_LOG_LEVEL))
                .map(level -> level.toUpperCase(Locale.ROOT))
                .ifPresent(Logging::setRootLoggerLevel);
    }

    public static void setRootLoggerLevel(@NotNull String rootLoggerLevel) {
        setRootLoggerLevel(Level.toLevel(rootLoggerLevel));
    }

    public static void setRootLoggerLevel(@Nullable Level rootLoggerLevel) {
        if (rootLoggerLevel != null) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(rootLoggerLevel);
        }
    }
}
