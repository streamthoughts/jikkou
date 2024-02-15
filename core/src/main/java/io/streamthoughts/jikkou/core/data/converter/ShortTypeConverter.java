/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data.converter;

import io.streamthoughts.jikkou.core.data.TypeConversionException;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import java.math.BigDecimal;

public final class ShortTypeConverter implements TypeConverter<Short> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public Short convertValue(Object value) {
        if (value == null) return null;

        if (value instanceof String string) {
            return new BigDecimal(string).shortValue();
        }
        if (value instanceof Number number) {
            return number.shortValue();
        }

        throw new TypeConversionException(String.format("Cannot parse 16-bits int content from \"%s\"", value));
    }
}
