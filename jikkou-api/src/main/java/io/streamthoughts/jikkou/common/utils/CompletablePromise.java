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
package io.streamthoughts.jikkou.common.utils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CompletablePromise<V> extends CompletableFuture<V> {

    private final CompletablePromiseContext context;
    private final Future<V> future;

    public CompletablePromise(Future<V> future, CompletablePromiseContext context) {
        this.future = Objects.requireNonNull(future, "future cannot be null");
        this.context = Objects.requireNonNull(context, "context cannot be null");
        context.schedule(this::tryToComplete);
    }

    private void tryToComplete() {
        if (future.isDone()) {
            try {
                complete(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                completeExceptionally(e);
            } catch (ExecutionException e) {
                completeExceptionally(e.getCause());
            }
            return;
        }

        if (future.isCancelled()) {
            cancel(true);
            return;
        }
        context.schedule(this::tryToComplete);
    }
}