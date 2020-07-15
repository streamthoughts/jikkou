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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 */
public class PropertiesUtils {

    public static Properties loadProps(final String path) throws IOException {
        return loadProps(path != null ? new File(path) : null);
    }
    public static Properties loadProps(final File f) throws IOException {
        Properties props = new Properties();
        try (FileInputStream is = new FileInputStream(f)) {
            props.load(is);
        }
        return props;
    }

    public static Map<String, String> toMap(Properties props) {
        final Map<String, String> map = new HashMap<>();
        for (final String name: props.stringPropertyNames())
            map.put(name, props.getProperty(name));
        return map;
    }

    public static Map<String, String> parse(List<String> props) {
        final Map<String, String> map = new HashMap<>();
        for (final String s : props) {
            if (!s.contains("=")) {
                throw new IllegalArgumentException("Invalid key/value property : " + s);
            }
            String[] pair = s.split("=", 0);
            map.put(pair[0], pair[1]);
        }
        return map;
    }

}
