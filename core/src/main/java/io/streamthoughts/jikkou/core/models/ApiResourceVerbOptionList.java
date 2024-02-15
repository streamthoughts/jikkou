/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * ApiResourceVerbOptionList.
 *
 * @param verb      the resource verb.
 * @param options   the options.
 */
@JsonPropertyOrder({
        "verb",
        "options"
})
@Reflectable
public record ApiResourceVerbOptionList(
        String verb,
        List<ApiOptionSpec> options) {

    @ConstructorProperties({
            "verb",
            "options"
    })
    public ApiResourceVerbOptionList {}

    public ApiResourceVerbOptionList(
            Verb verb,
            List<ApiOptionSpec> options) {
        this(verb.value(), options);
    }
}
