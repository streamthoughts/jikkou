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

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface CompletablePromiseContext extends AutoCloseable {

    static CompletablePromiseContext eventLoop() {
        return new Default(Executors.newSingleThreadScheduledExecutor(), Duration.ofMillis(1));
    }

    void schedule(Runnable r);

    void shutdown();

    /** {@inheritDoc} **/
    @Override
    default void close() {
        shutdown();
    }

    class Default implements CompletablePromiseContext {

        private final ScheduledExecutorService service;
        private final Duration interval;

        public Default(ScheduledExecutorService service, Duration interval) {
            this.service = service;
            this.interval = interval;
        }

        /** {@inheritDoc} **/
        @Override
        public void schedule(Runnable r) {
            service.schedule(r, interval.toMillis(), TimeUnit.MILLISECONDS);
        }

        /** {@inheritDoc} **/
        @Override
        public void shutdown() {
            service.shutdown();
        }
    }
}
