/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 */
public class PropertiesUtils {

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

    public static Map<String, String> toMap(final Properties props) {
        Map<String, String> builder = new LinkedHashMap<>();
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            builder.put(key, props.getProperty(key));
        }
        return builder;
    }
}
