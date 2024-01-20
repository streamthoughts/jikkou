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
package io.streamthoughts.jikkou.core.data;

import io.streamthoughts.jikkou.core.data.converter.BooleanTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.ByteBufferTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.DecimalTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.DoubleTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.FloatTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.IntegerTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.LongTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.ShortTypeConverter;
import io.streamthoughts.jikkou.core.data.converter.StringTypeConverter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Standard type converters.
 */
final class TypeConverters {

    static final TypeConverter<String> STRING_TYPE_CONVERTER = new StringTypeConverter();
    static final TypeConverter<Boolean> BOOLEAN_TYPE_CONVERTER = new BooleanTypeConverter();
    static final TypeConverter<Integer> INTEGER_TYPE_CONVERTER = new IntegerTypeConverter();
    static final TypeConverter<Short> SHORT_TYPE_CONVERTER = new ShortTypeConverter();
    static final TypeConverter<Long> LONG_TYPE_CONVERTER = new LongTypeConverter();
    static final TypeConverter<Float> FLOAT_TYPE_CONVERTER = new FloatTypeConverter();
    static final TypeConverter<Double> DOUBLE_TYPE_CONVERTER = new DoubleTypeConverter();
    static final TypeConverter<BigDecimal> DECIMAL_TYPE_CONVERTER = new DecimalTypeConverter();
    static final TypeConverter<ByteBuffer> BYTES_TYPE_CONVERTER = new ByteBufferTypeConverter();

    private TypeConverters() {
    }

}
