/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.internals;

import java.util.Map;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IcebergTypeMapperTest {

    // --- toIcebergType: primitive round-trip ---

    @ParameterizedTest(name = "toIcebergType(\"{0}\") should produce {1}")
    @CsvSource({
        "boolean,  BOOLEAN",
        "int,      INTEGER",
        "integer,  INTEGER",
        "long,     LONG",
        "float,    FLOAT",
        "double,   DOUBLE",
        "string,   STRING",
        "binary,   BINARY",
        "date,     DATE",
        "time,     TIME",
        "timestamp,TIMESTAMP",
        "timestamptz,TIMESTAMP",
        "uuid,     UUID"
    })
    void shouldMapPrimitivesToCorrectTypeId(String yamlType, String expectedTypeId) {
        Type result = IcebergTypeMapper.toIcebergType(yamlType);
        Assertions.assertEquals(
            Type.TypeID.valueOf(expectedTypeId),
            result.typeId(),
            "Type ID mismatch for: " + yamlType
        );
    }

    @Test
    void shouldMapTimestamptzWithZone() {
        Type result = IcebergTypeMapper.toIcebergType("timestamptz");
        Assertions.assertInstanceOf(Types.TimestampType.class, result);
        Assertions.assertTrue(((Types.TimestampType) result).shouldAdjustToUTC());
    }

    @Test
    void shouldMapTimestampWithoutZone() {
        Type result = IcebergTypeMapper.toIcebergType("timestamp");
        Assertions.assertInstanceOf(Types.TimestampType.class, result);
        Assertions.assertFalse(((Types.TimestampType) result).shouldAdjustToUTC());
    }

    @Test
    void shouldMapFixedType() {
        Type result = IcebergTypeMapper.toIcebergType("fixed[16]");
        Assertions.assertInstanceOf(Types.FixedType.class, result);
        Assertions.assertEquals(16, ((Types.FixedType) result).length());
    }

    @Test
    void shouldMapDecimalType() {
        Type result = IcebergTypeMapper.toIcebergType("decimal(10,2)");
        Assertions.assertInstanceOf(Types.DecimalType.class, result);
        Types.DecimalType dec = (Types.DecimalType) result;
        Assertions.assertEquals(10, dec.precision());
        Assertions.assertEquals(2, dec.scale());
    }

    @Test
    void shouldThrowForUnknownType() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> IcebergTypeMapper.toIcebergType("notavalidtype")
        );
    }

    // --- fromIcebergType: round-trip ---

    @ParameterizedTest(name = "fromIcebergType round-trip for \"{0}\"")
    @CsvSource({
        "boolean",
        "int",
        "long",
        "float",
        "double",
        "string",
        "binary",
        "date",
        "time",
        "timestamp",
        "timestamptz",
        "uuid"
    })
    void shouldRoundTripPrimitiveTypes(String yamlType) {
        Type iceberg = IcebergTypeMapper.toIcebergType(yamlType);
        String roundTripped = IcebergTypeMapper.fromIcebergType(iceberg);
        Assertions.assertEquals(yamlType, roundTripped,
            "Round-trip failed for: " + yamlType);
    }

    @Test
    void shouldRoundTripFixedType() {
        Type t = IcebergTypeMapper.toIcebergType("fixed[8]");
        Assertions.assertEquals("fixed[8]", IcebergTypeMapper.fromIcebergType(t));
    }

    @Test
    void shouldRoundTripDecimalType() {
        Type t = IcebergTypeMapper.toIcebergType("decimal(12,4)");
        Assertions.assertEquals("decimal(12,4)", IcebergTypeMapper.fromIcebergType(t));
    }

    // --- complex types ---

    @Test
    void shouldMapListType() {
        Type result = IcebergTypeMapper.toIcebergType(
            Map.of("type", "list", "elementType", "string")
        );
        Assertions.assertInstanceOf(Types.ListType.class, result);
        Assertions.assertEquals(Type.TypeID.STRING, ((Types.ListType) result).elementType().typeId());
    }

    @Test
    void shouldMapMapType() {
        Type result = IcebergTypeMapper.toIcebergType(
            Map.of("type", "map", "keyType", "string", "valueType", "int")
        );
        Assertions.assertInstanceOf(Types.MapType.class, result);
        Types.MapType mapType = (Types.MapType) result;
        Assertions.assertEquals(Type.TypeID.STRING, mapType.keyType().typeId());
        Assertions.assertEquals(Type.TypeID.INTEGER, mapType.valueType().typeId());
    }

    @Test
    void shouldThrowForMissingComplexTypeField() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> IcebergTypeMapper.toIcebergType(Map.of("type", "list")) // missing elementType
        );
    }
}
