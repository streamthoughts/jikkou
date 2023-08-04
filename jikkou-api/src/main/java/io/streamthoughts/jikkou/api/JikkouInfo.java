/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.api;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JikkouInfo} is used to get version information about the running library.
 */
public final class JikkouInfo {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouInfo.class);

    private static String VERSION = "unknown";

    static {
        try {
            Properties props = new Properties();
            props.load(JikkouInfo.class.getResourceAsStream("/jikkou-info.properties"));
            VERSION = props.getProperty("version", VERSION).trim();
        } catch (Exception e) {
            LOG.warn("Error while loading version: ", e);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}