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
package io.streamthoughts.jikkou.graal;

import com.hubspot.jinjava.el.ExtendedSyntaxBuilder;
import com.hubspot.jinjava.el.ext.eager.EagerExtendedSyntaxBuilder;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.lib.fn.TypeFunction;
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
            Set<Class<? extends Importable>> allSubTypes = reflections.getSubTypesOf(Importable.class);
            for (Class<? extends Importable> t : allSubTypes) {
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