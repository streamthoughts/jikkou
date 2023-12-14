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
package io.streamthoughts.jikkou.core.data.converter;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.math.BigDecimal;
import java.util.Optional;

public final class DecimalTypeConverter implements TypeConverter<BigDecimal> {

    /** {@inheritDoc} **/
    @Override
    public BigDecimal convertValue(Object value) {
        if (value == null) return null;

        String result = switch (value) {
            case Double ignored -> String.valueOf(value);
            case Integer ignored -> String.valueOf(value);
            case String ignored -> String.valueOf(value);
            case null, default -> throw newIllegalArgumentException(value);
        };

        if (result.trim().isEmpty()) {
            return null;
        }

        return getBigDecimal(value).orElseThrow(() -> newIllegalArgumentException(value));
    }

    private static RuntimeException newIllegalArgumentException(Object object) {
        return new IllegalArgumentException(String.format("Cannot parse decimal content from \"%s\"", object));
    }

    private static Optional<BigDecimal> getBigDecimal(final Object value) {
        try {
            return Optional.of(new BigDecimal(value.toString().replace(",", ".")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
