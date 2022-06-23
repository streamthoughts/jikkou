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
package io.streamthoughts.jikkou.api.error;

public class JikkouApiException extends JikkouException {

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param message   the detail message.
     */
    public JikkouApiException(final String message) {
        super(message);
    }

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param message   the detail message.
     * @param throwable the cause.
     */
    public JikkouApiException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param throwable the cause.
     */
    public JikkouApiException(final Throwable throwable) {
        super(throwable);
    }
}
