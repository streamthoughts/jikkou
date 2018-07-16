/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs.command;

import com.zenika.kafka.specs.resources.ClusterResource;

public enum TopicsCommands {

    UNKNOWN("Unknown operation on topic \"%s\""),
    CREATE("Create a new topic \"%s\""),
    DELETE("Delete topic \"%s\""),
    ALTER("Alter existing topic \"%s\"");

    private String format;

    /**
     * Creating a new TopicsCommands instance.
     * @param format
     */
    TopicsCommands(final String format) {
        this.format = format;
    }

    public String format(final ClusterResource resource) {
        return String.format(format, resource.name());
    }
}
