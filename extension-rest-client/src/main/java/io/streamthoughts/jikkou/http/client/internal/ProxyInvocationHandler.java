/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.internal;

import io.streamthoughts.jikkou.http.client.RestClientException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.jetbrains.annotations.NotNull;

public class ProxyInvocationHandler implements InvocationHandler {

    private static final Form EMPTY_FORM = new Form();
    public static final List<Object> EMPTY_COOKIES = Collections.emptyList();
    private final Client client;
    private final WebTarget target;
    private final WebResourceFactory webResourceFactory;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public static <C> C newResource(final Class<C> resourceInterface,
                                    final Client client,
                                    final WebTarget target,
                                    final MultivaluedMap<String, Object> headers
    ) {
        return (C) Proxy.newProxyInstance(resourceInterface.getClassLoader(),
                new Class[]{resourceInterface, AutoCloseable.class, Closeable.class},
                new ProxyInvocationHandler(client, addPathFromAnnotation(resourceInterface, target), headers));
    }

    /**
     * Creates a new {@link ProxyInvocationHandler} instance.
     *
     * @param client         the client
     * @param target         the webTarget
     * @param inboundHeaders the inbound http header
     */
    ProxyInvocationHandler(final @NotNull Client client,
                           final @NotNull WebTarget target,
                           final @NotNull MultivaluedMap<String, Object> inboundHeaders
    ) {
        this.client = client;
        this.target = target;
        try {
            Constructor<WebResourceFactory> constructor = WebResourceFactory.class.getDeclaredConstructor(
                    WebTarget.class,
                    MultivaluedMap.class,
                    List.class,
                    Form.class
            );
            constructor.setAccessible(true);
            webResourceFactory = constructor.newInstance(target, inboundHeaders, EMPTY_COOKIES, EMPTY_FORM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString") && (args == null || args.length == 0)) {
            return webResourceFactory.toString();
        }

        if (args == null && method.getName().equals("hashCode")) {
            return hashCode();
        }

        if (args != null && args.length == 1 && method.getName().equals("equals")) {
            if (!(args[0] instanceof Proxy)) {
                return false;
            }
            return equals(Proxy.getInvocationHandler(args[0]));
        }

        if (method.getName().equals("close") && (args == null || args.length == 0)) {
            close();
            return null;
        }

        if (closed.get()) {
            throw new IllegalStateException("Attempting to invoke a method on a closed client.");
        }

        try {
            return webResourceFactory.invoke(proxy, method, args);
        } catch (WebApplicationException e) {
            String httpMethodName = resolveHttpMethodName(method);
            Map<String, Object> templateValues = resolvePathParameters(method, args);
            String pathUrl = target.path(resolvePath(method)).resolveTemplates(templateValues).getUri().toString();
            e.getResponse().bufferEntity();
            throw new RestClientException(e, pathUrl, httpMethodName);
        } catch (RuntimeException e) {
            Throwable t = e.getCause();
            if (e.getCause() != null) {
                t = e.getCause();
            }
            throw new RestClientException(t);
        }
    }

    private void close() {
        if (closed.compareAndSet(false, true)) {
            client.close();
        }
    }

    private static Map<String, Object> resolvePathParameters(final Method method, Object[] args) {
        Map<String, Object> result = new HashMap<>();
        Annotation[][] paramAnns = method.getParameterAnnotations();
        for (int i = 0; i < paramAnns.length; i++) {
            final Map<Class<?>, Annotation> anns = new HashMap<>();
            for (final Annotation ann : paramAnns[i]) {
                anns.put(ann.annotationType(), ann);
            }
            PathParam pathParam = (PathParam) anns.get(PathParam.class);
            if (pathParam != null) {
                var value = args[i];
                Annotation ann;
                if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
                    value = ((DefaultValue) ann).value();
                }
                result.put(pathParam.value(), value);
            }
        }
        return result;
    }

    private static String resolvePath(final Method method) {
        Path a = method.getAnnotation(Path.class);
        return a == null ? null : a.value();
    }

    private static String resolveHttpMethodName(final Method method) {
        String httpMethod = getHttpMethodName(method);
        if (httpMethod == null) {
            for (final Annotation ann : method.getAnnotations()) {
                httpMethod = getHttpMethodName(ann.annotationType());
                if (httpMethod != null) {
                    break;
                }
            }
        }
        return httpMethod;
    }

    private static String getHttpMethodName(final AnnotatedElement ae) {
        final HttpMethod a = ae.getAnnotation(HttpMethod.class);
        return a == null ? null : a.value();
    }

    private static WebTarget addPathFromAnnotation(final AnnotatedElement ae, WebTarget target) {
        final Path p = ae.getAnnotation(Path.class);
        if (p != null) {
            target = target.path(p.value());
        }
        return target;
    }
}
