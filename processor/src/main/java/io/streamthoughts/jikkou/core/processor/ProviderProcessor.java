/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.processor;

import io.streamthoughts.jikkou.common.utils.Exceptions;
import io.streamthoughts.jikkou.core.annotation.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;


/**
 * Processes {@link Provider} annotations and generates the service provider
 * configuration files described in {@link java.util.ServiceLoader}.
 * <p>
 * Processor Options:<ul>
 * <li>{@code -Adebug} - turns on debug statements</li>
 * <li>{@code -Averify=true} - turns on extra verification</li>
 * </ul>
 */
@SupportedOptions({"debug", "verify"})
public class ProviderProcessor extends AbstractProcessor {

    public static final String PROVIDER_RESOURCE_FILE = ServicesFiles.getPath(io.streamthoughts.jikkou.spi.ExtensionProvider.class.getCanonicalName());

    private final List<String> exceptionStacks = Collections.synchronizedList(new ArrayList<>());

    /**
     * Contains all the class names of the concrete classes which implement the
     * {@link io.streamthoughts.jikkou.spi.ExtensionProvider} interface.
     */
    private final Set<String> providers = new HashSet<>();
    private javax.lang.model.util.Elements elementUtils;

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Provider.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * <ol>
     *  <li> For each class annotated with {@link Provider}<ul>
     *      <li> Verify the class is not abstract and implement the {@link Provider} interface.
     *      </ul>
     *
     * <li> Create a file named {@code META-INF/services/io.streamthoughts.jikkou.spi.ExtensionProvider}
     *       <li> For each {@link Provider} annotated class for this interface <ul>
     *           <li> Create an entry in the file
     *           </ul>
     *       </ul>
     * </ol>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            processImpl(annotations, roundEnv);
        } catch (RuntimeException e) {
            // We don't allow exceptions to any kind to propagate to the compiler
            String trace = Exceptions.getStackTraceAsString(e);
            exceptionStacks.add(trace);
            fatalError(trace);
        }
        return false;
    }

    private void processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateExtensionConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }
    }

    private void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Provider.class);

        log(annotations.toString());
        log(elements.toString());

        Element extensionInterface = elementUtils.getTypeElement(io.streamthoughts.jikkou.spi.ExtensionProvider.class.getCanonicalName());

        for (Element e : elements) {
            TypeElement extensionType = asTypeElement(e);
            Types types = processingEnv.getTypeUtils();

            // Checks whether the class annotated with @Extension
            // do implement the Extension interface, is not abstract, and defines a no-arg constructor.
            if (types.isSubtype(extensionType.asType(), extensionInterface.asType())
                && isNotAbstract(extensionType)
                && hasNoArgConstructor(extensionType)
            ) {
                log("extension provider: " + extensionType.getQualifiedName());
                providers.add(getBinaryName(extensionType));
            }
            // Otherwise just ignore the class.
        }
    }

    private void generateExtensionConfigFiles() {
        Filer filer = processingEnv.getFiler();
        log("Working on resource file: " + PROVIDER_RESOURCE_FILE);
        try {
            TreeSet<String> allServices = new TreeSet<>();
            try {
                // would like to be able to print the full path
                // before we attempt to get the resource in case the behavior
                // of filer.getResource does change to match the spec, but there's
                // no good way to resolve CLASS_OUTPUT without first getting a resource.
                FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", PROVIDER_RESOURCE_FILE);
                log("Looking for existing resource file at " + existingFile.toUri());
                Set<String> oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream());
                log("Existing service entries: " + oldServices);
                allServices.addAll(oldServices);
            } catch (IOException e) {
                // According to the javadoc, Filer.getResource throws an exception
                // if the file doesn't already exist. In practice this doesn't
                // appear to be the case. Filer.getResource will happily return a
                // FileObject that refers to a non-existent file but will throw
                // IOException if you try to open an input stream for it.
                log("Resource file did not already exist.");
            }

            if (!allServices.addAll(providers)) {
                log("No new service entries being added.");
                return;
            }

            log("New service file contents: " + allServices);
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", PROVIDER_RESOURCE_FILE);
            try (OutputStream out = fileObject.openOutputStream()) {
                ServicesFiles.writeServiceFile(allServices, out);
            }
            log("Wrote to: " + fileObject.toUri());
        } catch (IOException e) {
            fatalError("Unable to create " + PROVIDER_RESOURCE_FILE + ", " + e);
        }
    }

    /**
     * Returns the binary name of a reference type. For example,
     * {@code io.streamthoughts.Foo$Bar}, instead of {@code io.streamthoughts.Foo.Bar}.
     */
    private String getBinaryName(TypeElement element) {
        return getBinaryNameImpl(element, element.getSimpleName().toString());
    }

    private String getBinaryNameImpl(TypeElement element, String className) {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement pkg) {
            if (pkg.isUnnamed()) {
                return className;
            }
            return pkg.getQualifiedName() + "." + className;
        }

        TypeElement typeElement = asTypeElement(enclosingElement);
        return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
    }

    private static boolean isNotAbstract(final TypeElement extensionType) {
        return !extensionType.getModifiers().contains(Modifier.ABSTRACT);
    }

    private boolean hasNoArgConstructor(final TypeElement typeElement) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosedElement;
                if (constructorElement.getParameters().isEmpty()) {
                    // No-arg constructor found
                    return true;
                }
            }
        }
        return false;
    }

    private static TypeElement asTypeElement(Element enclosingElement) {
        return enclosingElement.accept(new SimpleElementVisitor8<TypeElement, Void>() {
            @Override
            public TypeElement visitType(TypeElement e, Void o) {
                return e;
            }
        }, null);
    }

    private void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void warning(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, element, annotation);
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
