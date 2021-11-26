/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.internal;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for waiting for enough kafka brokers to be ready.
 */
public class KafkaBrokersReady {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBrokersReady.class);

    public static class Options {
        private final int minAvailableBrokers;
        private final long timeoutMs;
        private final long retryBackoffMs;

        public static Options withDefaults() {
            return new Options();
        }

        public Options() {
            this(1, 60_000L, 1_000);
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
    }

    private final Options options;

    public KafkaBrokersReady(@Nonnull final Options options) {
        this.options = options;
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
                    if (isReady = !nodes.isEmpty() && numBrokerAvailable >= options.minAvailableBrokers) {
                        break;
                    }
                } catch (Exception e) {
                    LOG.error("Error while listing Kafka nodes: {}", e.getMessage());
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
        }
    }
}
