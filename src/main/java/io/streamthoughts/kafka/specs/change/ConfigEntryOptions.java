/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.change;

/**
 * Options used for computing config-entry changes.
 *
 * @see ConfigEntryChangeComputer
 */
public class ConfigEntryOptions extends ChangeComputer.Options {

    private final boolean deleteConfigOrphans;

    /**
     * Creates a new {@link ConfigEntryOptions} instance.
     */
    public ConfigEntryOptions() {
        this(false);
    }

    /**
     * Creates a new {@link ConfigEntryOptions} instance.
     *
     * @param deleteConfigOrphans {@code true} to allow orphan config-entries to be deleted, {@code false} otherwise.
     */
    private ConfigEntryOptions(final boolean deleteConfigOrphans) {
        this.deleteConfigOrphans = deleteConfigOrphans;
    }

    /**
     * @param deleteConfigOrphans   the value to be set.
     * @return  a new {@link TopicChangeOptions} with the given {@literal deleteConfigOrphans}.
     */
    public ConfigEntryOptions withDeleteConfigOrphans(final boolean deleteConfigOrphans) {
        return new ConfigEntryOptions(deleteConfigOrphans);
    }

    public boolean isDeleteConfigOrphans() {
        return deleteConfigOrphans;
    }
}
