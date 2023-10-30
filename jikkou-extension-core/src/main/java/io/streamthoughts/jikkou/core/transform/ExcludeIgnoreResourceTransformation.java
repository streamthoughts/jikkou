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
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Named("ExcludeIgnoreResource")
@Description("Excludes resources annotated with '" + CoreAnnotations.JIKKOU_IO_IGNORE + "'.")
@Enabled
@Priority(HasPriority.HIGHEST_PRECEDENCE)
public class ExcludeIgnoreResourceTransformation implements Transformation<HasMetadata> {

    /** {@inheritDoc}**/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform,
                                                    @NotNull HasItems resources) {
        return CoreAnnotations.isAnnotatedWithIgnore(toTransform) ?
                Optional.empty() : Optional.of(toTransform);
    }
}
