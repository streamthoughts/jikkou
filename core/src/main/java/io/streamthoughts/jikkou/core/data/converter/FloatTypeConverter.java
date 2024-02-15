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

public final class FloatTypeConverter implements TypeConverter<Float> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public Float convertValue(Object value) {
        if (value == null) return null;

        if (value instanceof String string) {
            return new BigDecimal(string).floatValue();
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }

        throw new TypeConversionException(String.format("Cannot parse 32-bits float content from \"%s\"", value));
    }
}
