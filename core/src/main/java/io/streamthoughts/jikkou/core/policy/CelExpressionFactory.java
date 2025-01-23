/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import com.google.protobuf.Struct;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelOptions;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.CelType;
import dev.cel.common.types.MapType;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.parser.CelStandardMacro;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import io.streamthoughts.jikkou.core.io.Jackson;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for building CEL expression.
 *
 * @param <T> the return expression type.
 */
public final class CelExpressionFactory<T> {

    private static final CelOptions CEL_OPTIONS = CelOptions
        .current()
        .enableCelValue(true)
        .build();

    private static final CelRuntime CEL_RUNTIME;

    public static final String RESOURCE_VAR = "resource";

    static {
        // CelRuntime takes in a compiled expression and produces an evaluable instance.
        // CelRuntime can also be initialized statically and cached just like the
        // compiler.
        CEL_RUNTIME = CelRuntimeFactory.standardCelRuntimeBuilder()
            .setOptions(CEL_OPTIONS)
            .build();
    }

    private static final CelExpressionFactory<Boolean> BOOLEAN_CEL_EXPR_FACTORY =
        new CelExpressionFactory<>(SimpleType.BOOL);

    private static final CelExpressionFactory<String> STRING_CEL_EXPR_FACTORY =
        new CelExpressionFactory<>(SimpleType.STRING);

    /**
     * Returns a {@link CelExpressionFactory} for compiling expression returning a boolean.
     *
     * @return the {@link CelExpressionFactory}.
     */
    public static CelExpressionFactory<Boolean> bool() {
        return BOOLEAN_CEL_EXPR_FACTORY;
    }

    /**
     * Returns a {@link CelExpressionFactory} for compiling expression returning a string.
     *
     * @return the {@link CelExpressionFactory}.
     */
    public static CelExpressionFactory<String> string() {
        return STRING_CEL_EXPR_FACTORY;
    }

    private final CelCompiler celCompiler;

    /**
     * Creates a new {@link CelExpressionFactory} instance.
     *
     * @param resultType The result type.
     */
    private CelExpressionFactory(final CelType resultType) {
        Objects.requireNonNull(resultType, "resultType cannot be null");

        celCompiler = CelCompilerFactory.standardCelCompilerBuilder()
            .setOptions(CEL_OPTIONS)
            .addVar(RESOURCE_VAR, MapType.create(SimpleType.STRING, SimpleType.DYN))
            .addMessageTypes(Struct.getDescriptor())
            .setStandardMacros(
                CelStandardMacro.ALL,
                CelStandardMacro.FILTER,
                CelStandardMacro.EXISTS,
                CelStandardMacro.EXISTS_ONE,
                CelStandardMacro.HAS,
                CelStandardMacro.MAP
            )
            .setResultType(resultType)
            .build();
    }

    /**
     * Compiles the given expression.
     *
     * @param expression The expression to compile
     * @return a new {@link CelExpression}.
     */
    @SuppressWarnings("unchecked")
    public CelExpression<T> compile(final @NotNull String expression) {

        CelAbstractSyntaxTree ast = compileExpression(expression);

        // Evaluate the program
        return resource -> {
            try {
                Map<String, Object> json = Jackson.json().convertValue(resource, Map.class);

                // Plan the program
                final CelRuntime.Program program;
                try {
                    program = CEL_RUNTIME.createProgram(ast);
                } catch (CelEvaluationException e) {
                    throw toIllegalArgumentExpression(expression, e);
                }

                return (T) program.eval(Map.of(RESOURCE_VAR, json));

            } catch (CelEvaluationException e) {
                throw toIllegalArgumentExpression(expression, e);
            }
        };
    }

    private static IllegalArgumentException toIllegalArgumentExpression(final @NotNull String expression,
                                                                        final CelEvaluationException e) {
        return new IllegalArgumentException("Evaluation error has occurred for expression '" + expression + "'. Reason: " + e.getMessage(), e);
    }

    private CelAbstractSyntaxTree compileExpression(final @NotNull String expression) {
        CelAbstractSyntaxTree ast;
        try {
            // Parse the expression
            ast = celCompiler.parse(expression).getAst();
        } catch (CelValidationException e) {
            // Report syntactic errors, if present
            throw new IllegalArgumentException(
                "Failed to compile expression: " + expression + ". Reason: " + e.getMessage(), e);
        }

        try {
            // Type-check the expression for correctness
            ast = celCompiler.check(ast).getAst();
        } catch (CelValidationException e) {
            // Report semantic errors, if present.
            throw new IllegalArgumentException(
                "Failed to type-check expression: " + expression + ". Reason: " + e.getMessage(), e);
        }
        return ast;
    }
}
