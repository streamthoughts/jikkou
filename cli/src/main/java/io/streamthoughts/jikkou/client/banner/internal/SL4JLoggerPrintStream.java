/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.banner.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class SL4JLoggerPrintStream extends PrintStream {

    private final Logger logger;

    private final Level level;

    private int last = -1;

    private final ByteArrayOutputStream bufOut;

    public SL4JLoggerPrintStream(final Logger logger, final Level level) {
        super(new ByteArrayOutputStream());
        bufOut = (ByteArrayOutputStream) super.out;
        this.logger = logger;
        this.level = level;
    }

    public void write(int b) {
        if ((last == '\r') && (b == '\n')) {
            last = -1;
            return;
        } else if ((b == '\n') || (b == '\r')) {
            try {
                final String message = bufOut.toString();
                logger.info(message);
            } finally {
                bufOut.reset();
            }
        } else {
            super.write(b);
        }
        last = b;
    }

    public void write(final byte b[], final  int off, final  int len) {
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException(len);
        }
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }
}