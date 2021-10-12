/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.internal;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Utility class for manipulating template using Mustache.
 */
public class MustacheUtil {

    private static final DefaultMustacheFactory MF = new DefaultMustacheFactory();

    public static String compile(final String template,
                                 final Object scope,
                                 final int recursive) {
        return compile(template, List.of(scope), recursive);
    }
    public static String compile(final String template,
                                 final List<Object> scopes,
                                 final int recursive) {
        if (recursive == 0) {
            return template;
        }

        Mustache compile = MF.compile(new StringReader(template), "");

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        compile.execute(pw, scopes);

        return compile(sw.getBuffer().toString(), scopes, recursive-1);
    }
}
