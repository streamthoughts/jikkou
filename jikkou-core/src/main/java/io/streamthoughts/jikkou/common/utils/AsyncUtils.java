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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for asynchronous.
 */
public class AsyncUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncUtils.class);
    public static final int DEFAULT_TIMEOUT = 30;

    public static <T> CompletableFuture<List<T>> waitForAll(Stream<CompletableFuture<List<T>>> futures) {
        return futures.reduce(CompletableFuture.completedFuture(new ArrayList<>()),
                (pre, curr) -> pre.thenCompose(preV -> curr.thenApply(currV -> {
                    preV.addAll(currV);
                    return preV;
                })));
    }


    public static<T> CompletableFuture<List<T>> waitForAll(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    public static <T> Either<T, Throwable> get(CompletableFuture<T> future) {
        T res;
        try {
            res = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Either.right(e);
        } catch (ExecutionException | TimeoutException e) {
            return Either.right(e.getCause());
        }
        return Either.left(res);
    }

    public static <T> Optional<T> getValue(CompletableFuture<T> future) {
        T res = null;
        try {
            res = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.error("getValue for async result failed", ex);
        }
        return Optional.ofNullable(res);
    }

    public static <T> Optional<Throwable> getException(CompletableFuture<T> future) {
        if (future != null && future.isCompletedExceptionally()) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.of(e);
            } catch (ExecutionException e) {
                return Optional.ofNullable(e.getCause());
            }
        }
        return Optional.empty();
    }

    public static <T> T getValueOrThrowException(CompletableFuture<T> future,
                                                 Function<Throwable, RuntimeException> mapper) {
        if (future != null) {
            try {
                return future.get();
            } catch (Exception e) {
                Throwable t = e;
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (e instanceof ExecutionException) {
                    if (e.getCause() != null) {
                        t = e.getCause();
                    }
                }
                throw mapper.apply(t);
            }
        }
        return null;
    }

    public static boolean isSuccessFuture(CompletableFuture<?> future) {
        return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
    }

    private AsyncUtils() {}

}
