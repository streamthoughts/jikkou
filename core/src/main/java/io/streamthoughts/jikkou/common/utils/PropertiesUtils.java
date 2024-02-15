/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to manipulate {@link Properties}.
 */
public final class PropertiesUtils {

    @NotNull
    public static Properties loadPropertiesConfig(@Nullable final File file) {
        final Properties props = new Properties();
        if (file != null) {
            if (!file.exists() || !file.canRead()) {
                throw new IllegalArgumentException(
                        "Invalid argument : File doesn't exist or is not readable : ' "
                                + file.getPath() + " ' "
                );
            }
            try {
                try (FileInputStream is = new FileInputStream(file)) {
                    props.load(is);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Invalid argument : File doesn't exist or is not readable : ' "
                                + file.getPath() + " ' "
                );
            }
        }
        return props;
    }

    public static Properties fromMap(final Map<String, Object> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    public static Map<String, Object> toMap(final Properties props) {
        Map<String, Object> builder = new LinkedHashMap<>();
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            builder.put(key, props.getProperty(key));
        }
        return builder;
    }
}
