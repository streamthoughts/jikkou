/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.transform;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.annotation.Named;
import io.jikkou.core.annotation.Priority;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.models.CoreAnnotations;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasPriority;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Named("ExcludeIgnoreResource")
@Title("ExcludeIgnoreResource allows filtering resources with an annotation '" +
        CoreAnnotations.JIKKOU_IO_IGNORE + "' equals to 'true'.")
@Description("The ExcludeIgnoreResource transformation is used to exclude from the "
        + "reconciliation process any resource whose 'metadata.annotations." + CoreAnnotations.JIKKOU_IO_IGNORE
        + "' annotation is equal to 'true'. This transformation is enabled by default."
)
@Enabled
@Priority(HasPriority.HIGHEST_PRECEDENCE)
public class ExcludeIgnoreResourceTransformation implements Transformation<HasMetadata> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                    @NotNull HasItems resources,
                                                    @NotNull ReconciliationContext context) {
        return CoreAnnotations.isAnnotatedWithIgnore(resource) ? Optional.empty() : Optional.of(resource);
    }
}
