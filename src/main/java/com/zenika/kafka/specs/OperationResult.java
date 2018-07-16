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
import com.zenika.kafka.specs.resources.ClusterResource;

import java.util.Arrays;

public class OperationResult<T extends ClusterResource> {

    public enum Status {

        CHANGED, OK, FAILED;
    }

    private final boolean changed;
    private final TopicsCommands cmd;
    private final long end;
    private final T resource;
    private final boolean failed;
    private final String[] error;


    public static <T extends ClusterResource> OperationResult<T> unchanged(final T resource, final TopicsCommands command) {
        return new OperationResult<>(false, resource, command, -1, false, null);
    }

    public static <T extends ClusterResource> OperationResult<T> changed(final T resource, final TopicsCommands command) {
        return new OperationResult<>(true, resource, command, -1, false, null);
    }

    /**
     * Creates a new {@link OperationResult} instance.
     *
     * @param changed
     * @param resource
     * @param cmd
     * @param end
     * @param error
     */
    public OperationResult(final boolean changed,
                           final T resource,
                           final TopicsCommands cmd,
                           final long end,
                           final boolean failed,
                           final String[] error) {
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
        if (isChanged()) return Status.CHANGED;
        else if (isFailed()) return Status.FAILED;
        else return Status.OK;
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
