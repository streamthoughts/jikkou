/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.printer;

import io.jikkou.core.models.ApiChangeResultList;
import io.jikkou.core.models.CoreAnnotations;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Operation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextPrinterTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void shouldGroupResultsByProvider_whenProviderAnnotationPresent() {
        ApiChangeResultList results = new ApiChangeResultList(false, new ObjectMeta(), List.of(
                ChangeResult.changed(changeWithOp(Operation.CREATE, "kafka-prod"), () -> "create topic on prod"),
                ChangeResult.changed(changeWithOp(Operation.UPDATE, "kafka-dev"), () -> "update topic on dev")
        ));

        int code = new TextPrinter(false).print(results, 100L, false);

        String output = captured.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(0, code);
        Assertions.assertTrue(output.contains("PROVIDER [kafka-prod]"), output);
        Assertions.assertTrue(output.contains("PROVIDER [kafka-dev]"), output);
        Assertions.assertTrue(output.indexOf("create topic on prod") > output.indexOf("PROVIDER [kafka-prod]"), output);
    }

    @Test
    void shouldPrintFlatOutput_whenNoProviderAnnotation() {
        ApiChangeResultList results = new ApiChangeResultList(false, new ObjectMeta(), List.of(
                ChangeResult.changed(changeWithOp(Operation.CREATE, null), () -> "create topic")
        ));

        int code = new TextPrinter(false).print(results, 100L, false);

        String output = captured.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(0, code);
        Assertions.assertFalse(output.contains("PROVIDER ["), output);
        Assertions.assertTrue(output.contains("create topic"), output);
        Assertions.assertTrue(output.contains("ok : 0, created : 1"), output);
    }

    @Test
    void shouldReturnNonZero_whenAnyChangeFailed() {
        ApiChangeResultList results = new ApiChangeResultList(false, new ObjectMeta(), List.of(
                ChangeResult.failed(changeWithOp(Operation.UPDATE, null), () -> "update topic", List.of())
        ));

        int code = new TextPrinter(false).print(results, 100L, false);

        Assertions.assertEquals(1, code);
    }

    private static ResourceChange changeWithOp(Operation op, String provider) {
        ObjectMeta.ObjectMetaBuilder meta = ObjectMeta.builder();
        if (provider != null) {
            meta = meta.withAnnotation(CoreAnnotations.JIKKOU_IO_PROVIDER, provider);
        }
        return GenericResourceChange.builder()
                .withMetadata(meta.build())
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(op)
                        .build()
                )
                .build();
    }
}
