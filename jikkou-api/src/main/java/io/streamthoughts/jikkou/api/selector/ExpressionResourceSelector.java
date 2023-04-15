/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.selector;

import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpressionResourceSelector implements ResourceSelector {

    private String key;

    private ExpressionOperator op;

    private List<String> values;

    private final ExpressionKeyValueExtractor keyExtractor;

    /**
     * Creates a new {@link ExpressionResourceSelector} instance.
     *
     * @param keyExtractor  the {@link ExpressionKeyValueExtractor}.
     */
    public ExpressionResourceSelector(final @NotNull ExpressionKeyValueExtractor keyExtractor) {
        this.keyExtractor = Objects.requireNonNull(
                keyExtractor, "'keyResourceExtractor' must not be null");
    }

    public ExpressionResourceSelector(final @NotNull ExpressionKeyValueExtractor keyExtractor,
                                      final @Nullable String key,
                                      final @NotNull ExpressionOperator op,
                                      final @NotNull List<String> values) {
        this.keyExtractor = Objects.requireNonNull(
                keyExtractor, "'keyResourceExtractor' must not be null");
        this.op = Objects.requireNonNull(op, "'op' must not be null");
        this.key = key;
        this.values = values;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOperator(ExpressionOperator op) {
        this.op = op;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean apply(@NotNull HasMetadata resource) {
        return op.create(key, values, keyExtractor).apply(resource);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "[" +
                "name=" + name() +
                "key=" + key +
                ", op=" + op +
                ", values=" + values +
                ']';
    }
}
