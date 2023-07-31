/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.handlers.acls;

import io.streamthoughts.jikkou.api.control.ChangeHandler;
import io.streamthoughts.jikkou.api.control.ChangeMetadata;
import io.streamthoughts.jikkou.api.control.ChangeResponse;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.kafka.adapters.KafkaAclBindingAdapter;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AclBinding;
import org.jetbrains.annotations.NotNull;

public class CreateAclChangeHandler implements KafkaAclChangeHandler {

    private final AdminClient adminClient;

    /**
     * Creates a new {@link CreateAclChangeHandler} instance.
     *
     * @param adminClient the {@link AdminClient}.
     */
    public CreateAclChangeHandler(@NotNull final AdminClient adminClient) {
        this.adminClient = Objects.requireNonNull(adminClient, "'adminClient should not be null'");
    }

    /** {@inheritDoc} */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD);
    }

    /** {@inheritDoc} */
    @Override
    public List<ChangeResponse<AclChange>> apply(@NotNull List<AclChange> changes) {
        Map<KafkaAclBinding, AclChange> data = changes
                .stream()
                .peek(c -> ChangeHandler.verify(this, c))
                .map(c -> new Tuple2<>(c.getAclBindings(), c))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

        List<AclBinding> bindings = data.keySet().stream()
                .map(KafkaAclBindingAdapter::toAclBinding)
                .toList();

        CreateAclsResult result = adminClient.createAcls(bindings);

        Map<AclBinding, KafkaFuture<Void>> results = result.values();

        return results.entrySet()
                .stream()
                .map(e -> new Tuple2<>(KafkaAclBindingAdapter.fromAclBinding(e.getKey()), e.getValue()))
                .map(t -> t.map2(Future::fromJavaFuture))
                .map(t -> t.map2(List::of))
                .map(t -> {
                    AclChange change = data.get(t._1());
                    List<CompletableFuture<ChangeMetadata>> futures = t._2().stream()
                            .map(Future::toCompletableFuture)
                            .map(f -> f.thenApply(unused -> ChangeMetadata.empty()))
                            .toList();
                    return new ChangeResponse<>(change, futures);
                })
                .toList();
    }
}
