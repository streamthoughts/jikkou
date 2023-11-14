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
package io.streamthoughts.jikkou.rest.exception;

import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class ApiResourceNotFoundException extends JikkouServerException {

    private final ApiResourceIdentifier identifier;

    public ApiResourceNotFoundException(@NotNull ApiResourceIdentifier identifier) {
        super(String.format(
                "Resource type for apiVersion '%s/%s' and name '%s' is unknown.",
                identifier.group(),
                identifier.version(),
                identifier.plural()
        ));
        this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
    }

    public ApiResourceIdentifier identifier() {
        return identifier;
    }
}
