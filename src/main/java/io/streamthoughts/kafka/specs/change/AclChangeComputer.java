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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AclChangeComputer implements ChangeComputer<AccessControlPolicy, AccessControlPolicy, AclChange, AclChangeOptions> {


    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> computeChanges(@NotNull final Iterable<AccessControlPolicy> actualStates,
                                          @NotNull final Iterable<AccessControlPolicy> expectedStates,
                                          @NotNull final AclChangeOptions options) {

        Map<String, List<AccessControlPolicy>> beforePoliciesGroupedByPrincipal = Named.groupByName(actualStates);
        Map<String, List<AccessControlPolicy>> afterPoliciesGroupedByPrincipal = Named.groupByName(expectedStates);

        final Map<String, List<AclChange>> changes = new HashMap<>();
        afterPoliciesGroupedByPrincipal.forEach((principal, afterPrincipalPolicies) -> {

            List<AccessControlPolicy> beforePrincipalPolicies = beforePoliciesGroupedByPrincipal.get(principal);

            List<AclChange> principalChanges = new LinkedList<>();

            if (beforePrincipalPolicies != null) {
                beforePrincipalPolicies.stream()
                        .filter(afterPrincipalPolicies::contains)
                        .map(AclChange::none)
                        .forEach(principalChanges::add);

                beforePrincipalPolicies.stream()
                        .filter(Predicate.not(afterPrincipalPolicies::contains))
                        .map(AclChange::delete)
                        .forEach(principalChanges::add);

                afterPrincipalPolicies.stream()
                        .filter(Predicate.not(beforePrincipalPolicies::contains))
                        .map(AclChange::add)
                        .forEach(principalChanges::add);
            } else {
                afterPrincipalPolicies.stream().map(AclChange::add).forEach(principalChanges::add);
            }

            changes.put(principal, principalChanges);

        });

        if (options.isDeleteOrphans()) {
            beforePoliciesGroupedByPrincipal.keySet()
                    .stream()
                    .filter(Predicate.not(changes::containsKey))
                    .forEach(principal -> changes.put(
                            principal,
                            beforePoliciesGroupedByPrincipal.get(principal)
                                    .stream()
                                    .map(AclChange::delete)
                                    .collect(Collectors.toList())
                    ));
        }
        return changes.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
