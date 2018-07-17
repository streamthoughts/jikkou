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
package com.zenika.kafka.specs;

import com.zenika.kafka.specs.command.TopicsCommands;
import com.zenika.kafka.specs.internal.Time;
import com.zenika.kafka.specs.resources.ClusterResource;

import java.util.Arrays;

public class OperationResult<T extends ClusterResource> {

    public enum Status {

        CHANGED, OK, FAILED, DRY_RUN
    }

    private final boolean changed;
    private final TopicsCommands cmd;
    private final long end;
    private final T resource;
    private final boolean failed;
    private final String[] error;
    private final Status status;

    /**
     * Build a new {@link OperationResult} that doesn't result in cluster resource changes.
     */
    public static <T extends ClusterResource> OperationResult<T> dryRun(final T resource,
                                                                        final boolean changed,
                                                                        final TopicsCommands command) {
        return new OperationResult<>(Status.DRY_RUN, changed, resource, command);
    }

    /**
     * Build a new {@link OperationResult} that doesn't result in cluster resource changes.
     */
    public static <T extends ClusterResource> OperationResult<T> unchanged(final T resource,
                                                                           final TopicsCommands command) {
        return new OperationResult<>(Status.OK, false, resource, command);
    }

    /**
     * Build a new {@link OperationResult} that do result in cluster resource changes.
     */
    public static <T extends ClusterResource> OperationResult<T> changed(final T resource,
                                                                         final TopicsCommands command) {
        return new OperationResult<>(Status.CHANGED, true, resource, command);
    }

    /**
     * Build a new {@link OperationResult} that failed with the specified exception.
     */
    public static <T extends ClusterResource> OperationResult<T> failed(final T resource,
                                                                        final TopicsCommands command,
                                                                        final Exception exception) {
        // TODO : Check whether an operation may fail after running some cluster resource changes ???
        return new OperationResult<>(Status.FAILED, false, resource, command, true, null);
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    private OperationResult(final Status status,
                            final boolean changed,
                            final T resource,
                            final TopicsCommands cmd) {
        this(status, changed, resource, cmd, false, null);
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    private OperationResult(final Status status,
                            final boolean changed,
                            final T resource,
                            final TopicsCommands cmd,
                            final boolean failed,
                            final String[] error) {
        this(status, changed, resource, cmd, failed, error, Time.SYSTEM.milliseconds());
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    public OperationResult(final Status status,
                           final boolean changed,
                           final T resource,
                           final TopicsCommands cmd,
                           final boolean failed,
                           final String[] error,
                           final long end) {
        this.status = status;
        this.changed = changed;
        this.resource = resource;
        this.cmd = cmd;
        this.end = end;
        this.failed = failed;
        this.error = error;
    }

    public boolean isChanged() {
        return changed;
    }

    public TopicsCommands getCmd() {
        return cmd;
    }

    public long getEnd() {
        return end;
    }

    public T getResource() {
        return resource;
    }

    public boolean isFailed() {
        return failed;
    }

    public String[] getError() {
        return error;
    }

    public Status status() {
        return this.status;
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "changed=" + changed +
                ", cmd='" + cmd + '\'' +
                ", end=" + end +
                ", resource=" + resource +
                ", failed=" + failed +
                ", error=" + Arrays.toString(error) +
                '}';
    }
}
