/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.internals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.jetbrains.annotations.NotNull;

/**
 * Maps between YAML/resource type strings and Apache Iceberg {@link Type} objects.
 */
public final class IcebergTypeMapper {

    private IcebergTypeMapper() {
        // utility class
    }

    /**
     * Converts a YAML type representation to an Iceberg {@link Type}.
     *
     * <p>The {@code typeObj} can be:
     * <ul>
     *   <li>A {@link String} for primitive types (e.g. "string", "long", "decimal(10,2)", "fixed[16]")</li>
     *   <li>A {@link Map} for complex types (struct, list, map)</li>
     * </ul>
     *
     * @param typeObj the type object from the YAML/JSON resource.
     * @return the corresponding Iceberg {@link Type}.
     * @throws IllegalArgumentException if the type is not recognized.
     */
    @NotNull
    public static Type toIcebergType(@NotNull final Object typeObj) {
        if (typeObj instanceof String s) {
            return parsePrimitiveType(s);
        } else if (typeObj instanceof Map<?, ?> map) {
            return parseComplexMapType(map);
        }
        throw new IllegalArgumentException("Unsupported type representation: " + typeObj);
    }

    /**
     * Converts an Iceberg {@link Type} back to its YAML string representation.
     *
     * @param type the Iceberg type.
     * @return a YAML-compatible string representation.
     */
    @NotNull
    public static String fromIcebergType(@NotNull final Type type) {
        return switch (type.typeId()) {
            case BOOLEAN -> "boolean";
            case INTEGER -> "int";
            case LONG -> "long";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            case STRING -> "string";
            case BINARY -> "binary";
            case DATE -> "date";
            case TIME -> "time";
            case UUID -> "uuid";
            case TIMESTAMP -> {
                Types.TimestampType ts = (Types.TimestampType) type;
                yield ts.shouldAdjustToUTC() ? "timestamptz" : "timestamp";
            }
            case FIXED -> {
                Types.FixedType fixed = (Types.FixedType) type;
                yield "fixed[" + fixed.length() + "]";
            }
            case DECIMAL -> {
                Types.DecimalType decimal = (Types.DecimalType) type;
                yield "decimal(" + decimal.precision() + "," + decimal.scale() + ")";
            }
            case STRUCT -> "struct";
            case LIST -> "list";
            case MAP -> "map";
            default -> throw new IllegalArgumentException("Unsupported Iceberg type: " + type);
        };
    }

    @NotNull
    private static Type parsePrimitiveType(@NotNull final String s) {
        return switch (s.toLowerCase().trim()) {
            case "boolean" -> Types.BooleanType.get();
            case "int", "integer" -> Types.IntegerType.get();
            case "long" -> Types.LongType.get();
            case "float" -> Types.FloatType.get();
            case "double" -> Types.DoubleType.get();
            case "string" -> Types.StringType.get();
            case "binary" -> Types.BinaryType.get();
            case "date" -> Types.DateType.get();
            case "time" -> Types.TimeType.get();
            case "timestamp" -> Types.TimestampType.withoutZone();
            case "timestamptz" -> Types.TimestampType.withZone();
            case "uuid" -> Types.UUIDType.get();
            default -> parseComplexStringType(s);
        };
    }

    @NotNull
    private static Type parseComplexStringType(@NotNull final String s) {
        // fixed[N]
        if (s.startsWith("fixed[") && s.endsWith("]")) {
            int n = Integer.parseInt(s.substring(6, s.length() - 1).trim());
            return Types.FixedType.ofLength(n);
        }
        // decimal(P,S)
        if (s.startsWith("decimal(") && s.endsWith(")")) {
            String inner = s.substring(8, s.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid decimal type: " + s + " — expected decimal(P,S)");
            }
            int precision = Integer.parseInt(parts[0].trim());
            int scale = Integer.parseInt(parts[1].trim());
            return Types.DecimalType.of(precision, scale);
        }
        throw new IllegalArgumentException("Unknown type string: '" + s + "'");
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static Type parseComplexMapType(@NotNull final Map<?, ?> map) {
        String typeName = (String) map.get("type");
        if (typeName == null) {
            throw new IllegalArgumentException("Complex type map is missing 'type' field: " + map);
        }
        return switch (typeName.toLowerCase().trim()) {
            case "struct" -> parseStructType((Map<String, Object>) map);
            case "list" -> parseListType((Map<String, Object>) map);
            case "map" -> parseMapType((Map<String, Object>) map);
            default -> throw new IllegalArgumentException("Unknown complex type: " + typeName);
        };
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static Types.StructType parseStructType(@NotNull final Map<String, Object> map) {
        List<Map<String, Object>> fields = (List<Map<String, Object>>) map.getOrDefault("fields", List.of());
        List<Types.NestedField> nestedFields = new ArrayList<>();
        int fieldId = 1;
        for (Map<String, Object> field : fields) {
            String name = (String) field.get("name");
            Object fieldType = field.get("type");
            boolean required = Boolean.TRUE.equals(field.getOrDefault("required", false));
            String doc = (String) field.get("doc");
            Type iceberg = toIcebergType(fieldType);
            Types.NestedField nestedField = required
                ? Types.NestedField.required(fieldId++, name, iceberg, doc)
                : Types.NestedField.optional(fieldId++, name, iceberg, doc);
            nestedFields.add(nestedField);
        }
        return Types.StructType.of(nestedFields);
    }

    @NotNull
    private static Types.ListType parseListType(@NotNull final Map<String, Object> map) {
        Object elementTypeObj = map.get("elementType");
        if (elementTypeObj == null) {
            throw new IllegalArgumentException("List type is missing 'elementType' field: " + map);
        }
        Type elementType = toIcebergType(elementTypeObj);
        boolean elementRequired = Boolean.TRUE.equals(map.getOrDefault("elementRequired", false));
        return elementRequired
            ? Types.ListType.ofRequired(1, elementType)
            : Types.ListType.ofOptional(1, elementType);
    }

    @NotNull
    private static Types.MapType parseMapType(@NotNull final Map<String, Object> map) {
        Object keyTypeObj = map.get("keyType");
        Object valueTypeObj = map.get("valueType");
        if (keyTypeObj == null || valueTypeObj == null) {
            throw new IllegalArgumentException("Map type is missing 'keyType' or 'valueType' field: " + map);
        }
        Type keyType = toIcebergType(keyTypeObj);
        Type valueType = toIcebergType(valueTypeObj);
        boolean valueRequired = Boolean.TRUE.equals(map.getOrDefault("valueRequired", false));
        return valueRequired
            ? Types.MapType.ofRequired(1, 2, keyType, valueType)
            : Types.MapType.ofOptional(1, 2, keyType, valueType);
    }
}
