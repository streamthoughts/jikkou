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

import com.fasterxml.jackson.core.type.TypeReference;
import io.streamthoughts.jikkou.core.data.converter.ObjectTypeConverter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Interface for converting Objects of one type to another.
 */
public interface TypeConverter<T> {

    /**
     * Converts the given value to another type.
     *
     * @param value The value to be converted
     * @return The converted value, or {@code null} if the passed value was {@code null}.
     *
     * @throws TypeConversionException if the value cannot be converted.
     */
    T convertValue(final Object value);

    /**
     * Returns a converter for converting an object to string.
     *
     * @return The converter for converting an object to string.
     */
    static TypeConverter<String> String() {
        return TypeConverters.STRING_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to boolean.
     *
     * @return The converter for converting an object to boolean.
     */
    static TypeConverter<Boolean> Boolean() {
        return TypeConverters.BOOLEAN_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to short.
     *
     * @return The converter for converting an object to short.
     */
    static TypeConverter<Short> Short() {
        return TypeConverters.SHORT_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to integer.
     *
     * @return The converter for converting an object to integer.
     */
    static TypeConverter<Integer> Integer() {
        return TypeConverters.INTEGER_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to long.
     *
     * @return The converter for converting an object to long.
     */
    static TypeConverter<Long> Long() {
        return TypeConverters.LONG_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to float.
     *
     * @return The converter for converting an object to float.
     */
    static TypeConverter<Float> Float() {
        return TypeConverters.FLOAT_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to double.
     *
     * @return The converter for converting an object to double.
     */
    static TypeConverter<Double> Double() {
        return TypeConverters.DOUBLE_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to decimal.
     *
     * @return The converter for converting an object to decimal.
     */
    static TypeConverter<BigDecimal> Decimal() {
        return TypeConverters.DECIMAL_TYPE_CONVERTER;
    }

    /**
     * Returns a converter for converting an object to bytes.
     *
     * @return The converter for converting an object to bytes.
     */
    static TypeConverter<ByteBuffer> Bytes() {
        return TypeConverters.BYTES_TYPE_CONVERTER;
    }
    
    /**
     * Returns a converter for converting an object to the given type.
     *
     * @param objectType The object type to convert into.
     * @param <T>        The type.
     * @return The converter for converting an object to the given type.
     */
    static <T> TypeConverter<T> of(Class<T> objectType) {
        return ObjectTypeConverter.newForType(objectType);
    }

    /**
     * Returns a converter for converting an object into a list of the given type.
     *
     * @param elementType The type to list elements.
     * @param <T>         The type of elements.
     * @return The converter for converting an object to a list of the given type.
     */
    static <T> TypeConverter<List<T>> ofList(Class<T> elementType) {
        return ObjectTypeConverter.newForList(elementType);
    }


    /**
     * Returns a converter for converting an object into a list of the given type.
     *
     * @return The converter for converting an object to a list of the given type.
     */
    static <K, V> TypeConverter<Map<K, V>> ofMap() {
        return ObjectTypeConverter.newForType(new TypeReference<>() {});
    }

}