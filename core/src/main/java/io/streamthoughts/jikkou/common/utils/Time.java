/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

public interface Time {

    Time SYSTEM = new SystemTime();

    /**
     * @return the current time in milliseconds.
     */
    long milliseconds();

    /**
     * A time implementation that uses the system clock and sleep call. Use `Time.SYSTEM` instead of creating an instance
     * of this class.
     */
    class SystemTime implements Time {

        /** {@inheritDoc} **/
        @Override
        public long milliseconds() {
            return System.currentTimeMillis();
        }
    }
}