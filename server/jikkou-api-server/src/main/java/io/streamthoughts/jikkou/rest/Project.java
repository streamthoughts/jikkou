/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest;

import io.streamthoughts.jikkou.rest.data.Info;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * A {@link Project} is used to get version information about the running library.
 */
public final class Project {

    private static Info INFO = Info.empty();

    static {
        URL resource = Project.class.getResource("/project-info.properties");
        if (resource != null) {
            try (Reader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                INFO = new Info(
                        props.getProperty("project.build.version"),
                        props.getProperty("project.build.time"),
                        props.getProperty("project.commit.id")
                );
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static Info info() {
        return INFO;
    }
}