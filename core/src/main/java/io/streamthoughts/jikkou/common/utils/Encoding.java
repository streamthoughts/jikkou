/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Utility class for encoding and decoding bytes array. */
public enum Encoding {
    HEX(Encoding::bytesToHex),
    UTF8(value -> new String(value, StandardCharsets.UTF_8)),
    ASCII(value -> new String(value, StandardCharsets.US_ASCII)),
    BASE64(
            value -> Base64.getEncoder().encodeToString(value),
            value -> Base64.getDecoder().decode(value)),
    BASE64_URL(
            value -> Base64.getUrlEncoder().encodeToString(value),
            value -> Base64.getUrlDecoder().decode(value));

    private final Function<byte[], String> encoder;
    private final Function<String, byte[]> decoder;

    /**
     * Creates a new {@link Encoding} instance.
     *
     * @param encoder the encoding function.
     */
    Encoding(final Function<byte[], String> encoder) {
        this(encoder, null);
    }

    /**
     * Creates a new {@link Encoding} instance.
     *
     * @param encoder the encoding function.
     */
    Encoding(final Function<byte[], String> encoder, final Function<String, byte[]> decoder) {
        this.encoder = Objects.requireNonNull(encoder, "'encode' should not be null");
        this.decoder = decoder;
    }

    public static Encoding from(final String value) {

        for (Encoding encoding : Encoding.values()) {
            if (value.equalsIgnoreCase(encoding.name())) {
                return encoding;
            }
        }

        String supported =
                Arrays.stream(Encoding.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(", ", "[ ", " ]"));

        throw new IllegalArgumentException(
                "Unknown encoding type '"
                        + value
                        + "'. "
                        + "Supported encoding types are: "
                        + supported
                        + ".");
    }

    public String encode(final byte[] data) {
        return encoder.apply(data);
    }

    public byte[] decode(final String data) {
        return Optional.ofNullable(decoder)
                .map(decoder -> decoder.apply(data))
                .orElseThrow(UnsupportedOperationException::new);
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /* https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java */
    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
