/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.graal;

import com.hubspot.jinjava.el.ExtendedSyntaxBuilder;
import com.hubspot.jinjava.el.ext.eager.EagerExtendedSyntaxBuilder;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.lib.fn.TypeFunction;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Set;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/**
 * Used for building native-image.
 */
class JinjaRuntimeReflectionRegistrationFeature implements Feature {

    public static final String JINJAVA_BASE_PACKAGE = "com.hubspot.jinjava";

    /** {@inheritDoc} **/
    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        try {
            Reflections reflections = new Reflections(JINJAVA_BASE_PACKAGE, Scanners.SubTypes);
            Set<Class<? extends Importable>> allImportableSubTypes = reflections.getSubTypesOf(Importable.class);
            for (Class<? extends Importable> t : allImportableSubTypes) {
                registerRuntimeReflection(t);
            }
            Set<Class<? extends PyWrapper>> allPyMapperSubTypes = reflections.getSubTypesOf(PyWrapper.class);
            for (Class<? extends PyWrapper> t : allPyMapperSubTypes) {
                registerRuntimeReflection(t);
            }
            registerRuntimeReflection(ExtendedSyntaxBuilder.class);
            registerRuntimeReflection(EagerExtendedSyntaxBuilder.class);
            registerRuntimeReflection(jinjava.de.odysseus.el.ExpressionFactoryImpl.class);

            RuntimeReflection.register(Functions.class);
            RuntimeReflection.register(Functions.class.getDeclaredMethods());
            RuntimeReflection.register(TypeFunction.class);
            RuntimeReflection.register(TypeFunction.class.getDeclaredMethod("type", Object.class));
            RuntimeReflection.register(com.google.common.collect.Lists.class);
            RuntimeReflection.register(com.google.common.collect.Lists.class.getDeclaredMethod("newArrayList", Object[].class));
        } catch (Exception e) {
            throw new RuntimeException("native-image build-time configuration failed", e);
        }
    }

    private static void registerRuntimeReflection(Class<?> t) {
        RuntimeReflection.register(t);
        RuntimeReflection.register(t.getDeclaredFields());
        RuntimeReflection.register(t.getDeclaredMethods());
        RuntimeReflection.register(t.getDeclaredConstructors());
    }
}