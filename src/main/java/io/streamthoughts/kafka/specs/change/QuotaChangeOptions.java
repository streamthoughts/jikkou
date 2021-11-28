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
 * Options used for computing quota changes.
 *
 * @see QuotaChangeComputer
 */
public class QuotaChangeOptions extends ChangeComputer.Options {

    private final boolean deleteConfigOrphans;

    private final boolean deleteQuotaOrphans;

    /**
     * Creates a new {@link QuotaChangeOptions} instance.
     */
    public QuotaChangeOptions() {
        this(false, false);
    }

    /**
     * Creates a new {@link QuotaChangeOptions} instance.
     *
     * @param deleteConfigOrphans {@code true} to allow orphan config-entries to be deleted, {@code false} otherwise.
     * @param deleteQuotaOrphans  {@code true} to allow orphan quotas to be deleted, {@code false} otherwise.
     */
    private QuotaChangeOptions(final boolean deleteConfigOrphans,
                               final boolean deleteQuotaOrphans) {
        this.deleteConfigOrphans = deleteConfigOrphans;
        this.deleteQuotaOrphans = deleteQuotaOrphans;
    }

    /**
     * @param deleteConfigOrphans   the value to set.
     * @return  a new {@link QuotaChangeOptions} with the given {@literal deleteConfigOrphans}
     */
    public QuotaChangeOptions withDeleteConfigOrphans(boolean deleteConfigOrphans) {
        return new QuotaChangeOptions(deleteConfigOrphans, deleteQuotaOrphans);
    }

    /**
     * @param deleteQuotaOrphans   the value to set.
     * @return  a new {@link QuotaChangeOptions} with the given {@literal deleteQuotaOrphans}
     */
    public QuotaChangeOptions withDeleteQuotaOrphans(boolean deleteQuotaOrphans) {
        return new QuotaChangeOptions(deleteConfigOrphans, deleteQuotaOrphans);
    }

    public boolean isDeleteConfigOrphans() {
        return deleteConfigOrphans;
    }

    public boolean isDeleteQuotaOrphans() {
        return deleteQuotaOrphans;
    }
}

