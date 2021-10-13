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
package io.streamthoughts.kafka.specs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.kafka.specs.internal.Time;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * This class is used to describe the result of an operation that succeed of failed.
 *
 * @param <T>   the operation result-type.
 */
public class OperationResult<T> implements Serializable {

    public enum Status { CHANGED, OK, FAILED }

    private final boolean changed;
    private final long end;
    private final T resource;
    private final boolean failed;
    private final String error;
    private final Status status;
    private transient final Description description;

    /**
     * Static method to build a new {@link OperationResult} that doesn't result in cluster resource changes.
     *
     * @param resource      the operation result.
     * @param description   the operation result description.
     * @param <T>           the operation result-type.
     *
     * @return              a new {@link OperationResult}.
     */
    public static <T> OperationResult<T> ok(final T resource,
                                            final Description description) {
        return new OperationResult<>(Status.OK, false, resource, description);
    }

    /**
     * Static method to build a new {@link OperationResult} that do result in cluster resource changes.
     *
     * @param resource      the operation result.
     * @param description   the operation result description.
     * @param <T>           the operation result-type.
     *
     * @return              a new {@link OperationResult}.
     */
    public static <T> OperationResult<T> changed(final T resource,
                                                 final Description description) {
        return new OperationResult<>(Status.CHANGED, true, resource, description);
    }

    /**
     * Static method to build a new {@link OperationResult}  that failed with the specified exception.
     *
     * @param resource      the operation result.
     * @param description   the operation result description.
     * @param exception     the exception.
     * @param <T>           the operation result-type.
     *
     * @return              a new {@link OperationResult}.
     */
    public static <T> OperationResult<T> failed(final T resource,
                                                final Description description,
                                                final Exception exception) {
        return new OperationResult<>(
                Status.FAILED,
                false,
                resource,
                description,
                true,
                getStacktrace(exception)
        );
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    private OperationResult(final Status status,
                            final boolean changed,
                            final T resource,
                            final Description description) {
        this(status, changed, resource, description, false, null);
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    private OperationResult(final Status status,
                            final boolean changed,
                            final T resource,
                            final Description description,
                            final boolean failed,
                            final String error) {
        this(status, changed, resource, description, failed, error, Time.SYSTEM.milliseconds());
    }

    /**
     * Creates a new {@link OperationResult} instance.
     */
    private OperationResult(final Status status,
                            final boolean changed,
                            final T resource,
                            final Description description,
                            final boolean failed,
                            final String error,
                            final long end) {
        this.status = status;
        this.changed = changed;
        this.resource = resource;
        this.end = end;
        this.failed = failed;
        this.error = error;
        this.description = description;
    }

    @JsonProperty
    public boolean isChanged() {
        return changed;
    }

    @JsonProperty
    public long getEnd() {
        return end;
    }

    @JsonProperty
    public T resource() {
        return resource;
    }

    @JsonProperty
    public boolean isFailed() {
        return failed;
    }

    @JsonProperty
    public String error() {
        return error;
    }

    @JsonProperty
    public Status status() {
        return this.status;
    }

    @JsonIgnore
    public Description description() {
        return this.description;
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "changed=" + changed +
                ", end=" + end +
                ", resource=" + resource +
                ", failed=" + failed +
                ", error=" + error +
                ", status=" + status +
                ", description=" + description +
                '}';
    }

    /**
     * @return the stacktrace representation for the given {@link Throwable}.
     */
    static String getStacktrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
