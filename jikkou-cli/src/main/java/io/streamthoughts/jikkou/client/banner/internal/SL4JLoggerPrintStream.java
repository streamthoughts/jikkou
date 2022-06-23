/*
 * Copyright 2022 StreamThoughts.
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