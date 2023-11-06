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
package io.streamthoughts.jikkou.kafka.internals;

import io.streamthoughts.jikkou.common.utils.Time;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.common.Node;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for waiting for enough kafka brokers to be ready.
 */
public final class KafkaBrokersReady {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBrokersReady.class);

    public static final int DEFAULT_MIN_AVAILABLE_BROKERS = 1;
    public static final long DEFAULT_TIMEOUT_MS = 60_000L;
    public static final long DEFAULT_RETRY_BACKOFF_MS = 1_000L;

    public static class Options {
        private final int minAvailableBrokers;
        private final long timeoutMs;
        private final long retryBackoffMs;

        public static Options withDefaults() {
            return new Options();
        }

        public Options() {
            this(DEFAULT_MIN_AVAILABLE_BROKERS, DEFAULT_TIMEOUT_MS, DEFAULT_RETRY_BACKOFF_MS);
        }

        public Options(final int minAvailableBrokers,
                       final long timeoutMs,
                       final long retryBackoffMs) {
            this.minAvailableBrokers = minAvailableBrokers;
            this.timeoutMs = timeoutMs;
            this.retryBackoffMs = retryBackoffMs;
        }

        public Options withMinAvailableBrokers(final int minAvailableBrokers) {
            return new Options(minAvailableBrokers, timeoutMs, retryBackoffMs);
        }

        public Options withTimeoutMs(final long timeoutMs) {
            return new Options(minAvailableBrokers, timeoutMs, retryBackoffMs);
        }

        public Options withRetryBackoffMs(final long retryBackoffMs) {
            return new Options(minAvailableBrokers, timeoutMs, retryBackoffMs);
        }

        public int minAvailableBrokers() {
            return minAvailableBrokers;
        }

        public long timeoutMs() {
            return timeoutMs;
        }

        public long retryBackoffMs() {
            return retryBackoffMs;
        }
    }

    private final Options options;

    public KafkaBrokersReady(@NotNull final Options options) {
        this.options = options;
    }

    public KafkaBrokersReady() {
        this(Options.withDefaults());
    }

    public boolean waitForBrokers(final AdminClient adminClient) {
        boolean isReady = false;
        int numBrokerAvailable = 0;
        try {
            LOG.info("Checking for Kafka to be ready. Expected broker(s): {}", options.minAvailableBrokers);

            final var start = Time.SYSTEM.milliseconds();
            var remaining = options.timeoutMs;

            while (remaining > 0) {
                try {
                    Collection<Node> nodes = getClusterNodes(adminClient, remaining);
                    numBrokerAvailable = nodes.size();
                    isReady = !nodes.isEmpty() && numBrokerAvailable >= options.minAvailableBrokers;
                    if (isReady) {
                        break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    LOG.error("Error while listing Kafka nodes: {}", e.getMessage());
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
                sleep(Duration.ofMillis(Math.min(options.retryBackoffMs, remaining(start, options.timeoutMs))));

                LOG.info("Waiting for Kafka cluster to be ready. Expected {} brokers, but only {} was found.",
                        options.minAvailableBrokers,
                        numBrokerAvailable
                );
                remaining = remaining(start, options.timeoutMs);
            }
            return isReady;
        } finally {
            if (!isReady) {
                LOG.warn(
                    "Timeout expired. Kafka cluster is not ready yet. Expected {} brokers, but only {} were available.",
                    options.minAvailableBrokers,
                    numBrokerAvailable
                );
            }
        }
    }

    private long remaining(final long start, final long timeoutMs) {
        return Math.max(0, timeoutMs - (Time.SYSTEM.milliseconds() - start));
    }

    private Collection<Node> getClusterNodes(final AdminClient adminClient,
                                             final long remaining) throws InterruptedException, ExecutionException {
        return adminClient
                .describeCluster(timeoutMsOptions(remaining))
                .nodes()
                .get();
    }

    private static DescribeClusterOptions timeoutMsOptions(final long remaining) {
        return new DescribeClusterOptions().timeoutMs((int) Math.min(Integer.MAX_VALUE, remaining));
    }

    public void sleep(final Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
