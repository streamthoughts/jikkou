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
package io.streamthoughts.jikkou.kafka.change;

/**
 * Options used for computing topic changes.
 *
 * @see TopicChangeComputer
 */
public class TopicChangeOptions extends ChangeComputer.Options {

    private final boolean deleteConfigOrphans;

    private final boolean deleteTopicOrphans;

    private final boolean excludeInternalTopics;

    /**
     * Creates a new {@link TopicChangeOptions} instance.
     */
    public TopicChangeOptions() {
        this(false, false, true);
    }

    /**
     * Creates a new {@link TopicChangeOptions} instance.
     *
     * @param deleteConfigOrphans    {@code true} to allow orphan config-entries to be deleted, {@code false} otherwise.
     * @param deleteTopicOrphans     {@code true} to allow orphan topics to be deleted, {@code false} otherwise.
     * @param excludeInternalTopics  {@code true} to exclude internal topics from being deleted, {@code false} otherwise.
     */
    private TopicChangeOptions(final boolean deleteConfigOrphans,
                               final boolean deleteTopicOrphans,
                               final boolean excludeInternalTopics) {
        this.deleteConfigOrphans = deleteConfigOrphans;
        this.deleteTopicOrphans = deleteTopicOrphans;
        this.excludeInternalTopics = excludeInternalTopics;
    }

    /**
     * @param deleteConfigOrphans   the value to be set.
     * @return  a new {@link TopicChangeOptions} with the given {@literal deleteConfigOrphans}.
     */
    public TopicChangeOptions withDeleteConfigOrphans(boolean deleteConfigOrphans) {
        return new TopicChangeOptions(deleteConfigOrphans, deleteTopicOrphans, excludeInternalTopics);
    }

    /**
     * @param deleteTopicOrphans    the value to be set.
     * @return  a new {@link TopicChangeOptions} with the given {@literal deleteTopicOrphans}.
     */
    public TopicChangeOptions withDeleteTopicOrphans(boolean deleteTopicOrphans) {
        return new TopicChangeOptions(deleteConfigOrphans, deleteTopicOrphans, excludeInternalTopics);
    }

    /**
     * @param excludeInternalTopics  the value to be set.
     * @return  a new {@link TopicChangeOptions} with the given {@literal deleteTopicOrphans}.
     */
    public TopicChangeOptions withExcludeInternalTopics(boolean excludeInternalTopics) {
        return new TopicChangeOptions(deleteConfigOrphans, deleteTopicOrphans, excludeInternalTopics);
    }

    public boolean isDeleteConfigOrphans() {
        return deleteConfigOrphans;
    }

    public boolean isDeleteTopicOrphans() {
        return deleteTopicOrphans;
    }

    public boolean isExcludeInternalTopics() {
        return excludeInternalTopics;
    }
}

