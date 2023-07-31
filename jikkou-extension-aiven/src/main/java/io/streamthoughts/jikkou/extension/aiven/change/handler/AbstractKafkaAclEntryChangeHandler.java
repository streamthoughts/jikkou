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
package io.streamthoughts.jikkou.extension.aiven.change.handler;

import io.streamthoughts.jikkou.api.control.ChangeError;
import io.streamthoughts.jikkou.api.control.ChangeHandler;
import io.streamthoughts.jikkou.api.control.ChangeMetadata;
import io.streamthoughts.jikkou.api.control.ChangeResponse;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.ErrorsResponse;
import io.streamthoughts.jikkou.extension.aiven.api.restclient.RestClientResponseException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractKafkaAclEntryChangeHandler<T> implements ChangeHandler<ValueChange<T>> {

    protected final AivenApiClient api;

    protected final ChangeType changeType;

    /**
     * Creates a new {@link AbstractKafkaAclEntryChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public AbstractKafkaAclEntryChangeHandler(@NotNull final AivenApiClient api,
                                              @NotNull final ChangeType changeType) {
        this.api = Objects.requireNonNull(api, "api must not be null");
        this.changeType = Objects.requireNonNull(changeType, "changeType must not be null");
    }

    protected <R> ChangeResponse<ValueChange<T>> executeAsync(final ValueChange<T> change,
                                                              final Supplier<R> supplier) {
        CompletableFuture<ChangeMetadata> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        supplier.get();
                        return ChangeMetadata.empty();
                    } catch (RestClientResponseException e) {
                        try {
                            ErrorsResponse entity = e.getResponseEntity(ErrorsResponse.class);
                            if (entity.errors().size() == 1) {
                                ErrorsResponse.Error error = entity.errors().get(0);
                                return new ChangeMetadata(new ChangeError(error.message(), error.status()));
                            } else {
                                return new ChangeMetadata(new ChangeError(entity.message()));
                            }
                        } catch (Exception ignore) {
                            return ChangeMetadata.of(e);
                        }
                    }
                });
        return new ChangeResponse<>(change, future);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(changeType);
    }
}
