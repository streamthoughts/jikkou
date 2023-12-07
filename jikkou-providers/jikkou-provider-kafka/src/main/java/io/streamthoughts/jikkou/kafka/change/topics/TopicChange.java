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
package io.streamthoughts.jikkou.kafka.change.topics;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import java.util.stream.Collectors;

public final class TopicChange {

    public static final String PARTITIONS = "partitions";
    public static final String REPLICAS = "replicas";
    public static final String CONFIG_PREFIX = "config.";

    public static TextDescription getDescription(ResourceChange change) {
        return () -> {

            StateChangeList<? extends StateChange> data = change.getSpec().getChanges();

            String configs = data
                    .allWithPrefix(CONFIG_PREFIX)
                    .stream()
                    .map(it -> Pair.of(it.getName(), it))
                    .map(pair -> pair._1() + "=" + pair._2().getAfter())
                    .collect(Collectors.joining(","));

            return String.format("%s topic '%s' (partitions=%d, replicas=%d, configs=[%s])",
                    change.getSpec().getOp().humanize(),
                    change.getMetadata().getName(),
                    data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
                    data.getLast(REPLICAS, TypeConverter.Short()).getAfter(),
                    configs
            );
        };
    }
}
