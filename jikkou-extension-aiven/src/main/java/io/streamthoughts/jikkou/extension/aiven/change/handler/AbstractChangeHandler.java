/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeError;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResponse;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.MessageErrorsResponse;
import io.streamthoughts.jikkou.http.client.RestClientException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractChangeHandler<T> implements ChangeHandler<ValueChange<T>> {

    protected final AivenApiClient api;

    protected final Set<ChangeType> supportedChangeTypes;

    /**
     * Creates a new {@link AbstractChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public AbstractChangeHandler(@NotNull final AivenApiClient api,
                                 @NotNull final ChangeType changeType) {
        this(api, Set.of(changeType));
    }

    /**
     * Creates a new {@link AbstractChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public AbstractChangeHandler(@NotNull final AivenApiClient api,
                                 @NotNull final Set<ChangeType> supportedChangeTypes) {
        this.api = Objects.requireNonNull(api, "api must not be null");
        this.supportedChangeTypes = Objects.requireNonNull(supportedChangeTypes, "changeType must not be null");
    }

    protected <R> ChangeResponse<ValueChange<T>> executeAsync(final HasMetadataChange<ValueChange<T>> change,
                                                              final Supplier<R> supplier) {
        CompletableFuture<ChangeMetadata> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        supplier.get();
                        return ChangeMetadata.empty();
                    } catch (RestClientException e) {
                        try {
                            MessageErrorsResponse entity = e.getResponseEntity(MessageErrorsResponse.class);
                            if (entity.errors().size() == 1) {
                                MessageErrorsResponse.Error error = entity.errors().get(0);
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
        return supportedChangeTypes;
    }
}
