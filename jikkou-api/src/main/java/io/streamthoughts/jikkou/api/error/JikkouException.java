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
package io.streamthoughts.jikkou.api.error;

/**
 * Top-level exception for Kafka Specs.
 */
public class JikkouException extends RuntimeException {

    public JikkouException() {
        super();
    }

    public JikkouException(final String message) {
        super(message);
    }

    public JikkouException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public JikkouException(final Throwable cause) {
        super(cause);
    }

}
