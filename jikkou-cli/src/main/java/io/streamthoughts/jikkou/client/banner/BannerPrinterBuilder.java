/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.client.banner;
import io.streamthoughts.jikkou.client.banner.internal.SL4JLoggerPrintStream;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Default class for building {@link BannerPrinter} instance.
 */
public final class BannerPrinterBuilder {

    private Logger logger;

    private Level level;

    private Banner.Mode mode;

    public static BannerPrinterBuilder newBuilder() {
        return new BannerPrinterBuilder();
    }

    /**
     * Creates a new {@link BannerPrinterBuilder} instance.
     */
    private BannerPrinterBuilder() {

    }

    public BannerPrinterBuilder setLoggerLevel(final Level level) {
        this.level = level;
        return this;
    }

    public BannerPrinterBuilder setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public BannerPrinterBuilder setMode(final Banner.Mode mode) {
        this.mode = mode;
        return this;
    }

    public BannerPrinter build() {
        if (mode == null) {
            throw new IllegalStateException("Cannot build a new builder with no defined mode");
        }
        if (mode == Banner.Mode.CONSOLE) {
            return new DefaultBannerPrinter(System.out);
        }

        if (mode == Banner.Mode.LOG) {
            if (logger == null) {
                throw new IllegalStateException("Cannot build a new builder (mode=log) with an empty logger");
            }
            return new DefaultBannerPrinter(new SL4JLoggerPrintStream(logger, level));
        }
        // mode == Mode.OFF
        return BannerPrinter.NO_OP;
    }
}