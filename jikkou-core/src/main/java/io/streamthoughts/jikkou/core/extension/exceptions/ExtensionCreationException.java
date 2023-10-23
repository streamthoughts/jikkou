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
package io.streamthoughts.jikkou.core.extension.exceptions;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

/**
 * Indicates that a checked exception was thrown during creation of an extension.
 */
public class ExtensionCreationException extends JikkouRuntimeException {

    /**
     * Creates a new {@link ExtensionCreationException} instance.
     *
     * @param cause the cause.
     */
    public ExtensionCreationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new {@link ExtensionCreationException} instance.
     *
     * @param message the error message.
     */
    public ExtensionCreationException(String message) {
        super(message);
    }
}
