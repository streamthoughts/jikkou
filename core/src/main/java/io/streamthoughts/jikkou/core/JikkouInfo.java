/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JikkouInfo} is used to get version information about the running library.
 */
public final class JikkouInfo {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouInfo.class);

    private static String VERSION = "unknown";

    private static String BUILD_TIMESTAMP;

    static {
        try {
            Properties props = new Properties();
            props.load(JikkouInfo.class.getResourceAsStream("/jikkou-info.properties"));
            VERSION = props.getProperty("version", VERSION).trim();
            BUILD_TIMESTAMP = props.getProperty("build.timestamp");
        } catch (Exception e) {
            LOG.warn("Error while loading version: ", e);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
    public static String getBuildTimestamp() {
        return BUILD_TIMESTAMP;
    }
}