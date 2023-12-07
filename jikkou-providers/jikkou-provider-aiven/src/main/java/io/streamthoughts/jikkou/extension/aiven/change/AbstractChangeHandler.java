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
package io.streamthoughts.jikkou.extension.aiven.change;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeError;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.MessageErrorsResponse;
import io.streamthoughts.jikkou.http.client.RestClientException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractChangeHandler extends BaseChangeHandler<ResourceChange> {

    protected final AivenApiClient api;

    /**
     * Creates a new {@link AbstractChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public AbstractChangeHandler(@NotNull final AivenApiClient api,
                                 @NotNull final Operation operation) {
        this(api, Set.of(operation));
    }

    /**
     * Creates a new {@link AbstractChangeHandler} instance.
     *
     * @param api the {@link AivenApiClient} instance.
     */
    public AbstractChangeHandler(@NotNull final AivenApiClient api,
                                 @NotNull final Set<Operation> supportedOperations) {
        super(supportedOperations);
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    protected <R> ChangeResponse<ResourceChange> executeAsync(final ResourceChange change,
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
                                MessageErrorsResponse.Error error = entity.errors().getFirst();
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

    public static <T> T getEntry(ResourceChange change, Class<T> entryType) {
        return change.getSpec()
                .getChanges()
                .getLast("entry", TypeConverter.of(entryType))
                .getAfter();
    }
}
