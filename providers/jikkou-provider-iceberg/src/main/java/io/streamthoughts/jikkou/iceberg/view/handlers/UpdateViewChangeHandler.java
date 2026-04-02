/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.view.handlers;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import io.streamthoughts.jikkou.iceberg.view.ViewChangeDescription;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergViewQuery;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.catalog.ViewCatalog;
import org.apache.iceberg.view.ReplaceViewVersion;
import org.apache.iceberg.view.UpdateViewProperties;
import org.apache.iceberg.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for updating Iceberg views.
 *
 * <p>View updates work by creating a new ViewVersion via {@code replaceVersion()}.
 * The schema is always preserved from the live view (engine-inferred).
 * Properties are updated independently via {@code updateProperties()}.
 */
public final class UpdateViewChangeHandler extends BaseChangeHandler {

    private static final String PROPERTY_PREFIX = "property.";

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link UpdateViewChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public UpdateViewChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.UPDATE);
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return new ViewChangeDescription(change);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<ChangeResponse> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
            .map(change -> {
                CompletableFuture<ChangeMetadata> future = CompletableFuture.supplyAsync(() -> {
                    Catalog catalog = catalogFactory.createCatalog();
                    if (!(catalog instanceof ViewCatalog viewCatalog)) {
                        throw new JikkouRuntimeException(
                            "The configured catalog does not support views (does not implement ViewCatalog)");
                    }

                    String resourceName = change.getMetadata().getName();
                    TableIdentifier identifier = parseIdentifier(resourceName);
                    View view = viewCatalog.loadView(identifier);

                    var stateChanges = change.getSpec().getChanges();

                    // Check if version-level changes exist (queries or defaults changed)
                    boolean hasVersionChanges = stateChanges.stream()
                        .anyMatch(sc -> ("queries".equals(sc.getName())
                            || "defaultNamespace".equals(sc.getName())
                            || "defaultCatalog".equals(sc.getName()))
                            && sc.getOp() != Operation.NONE);

                    if (hasVersionChanges) {
                        ReplaceViewVersion replace = view.replaceVersion();

                        // Always use the live schema (engine-inferred)
                        replace.withSchema(view.schema());

                        // Apply the "after" queries
                        for (StateChange sc : stateChanges) {
                            if ("queries".equals(sc.getName())) {
                                Object queriesObj = sc.getOp() != Operation.NONE
                                    ? sc.getAfter() : sc.getBefore();
                                if (queriesObj instanceof List<?> queryList) {
                                    for (Object q : queryList) {
                                        if (q instanceof V1IcebergViewQuery query) {
                                            replace.withQuery(query.getDialect(), query.getSql());
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        // Apply default namespace
                        String afterNs = null;
                        for (StateChange sc : stateChanges) {
                            if ("defaultNamespace".equals(sc.getName())) {
                                afterNs = sc.getOp() != Operation.NONE
                                    ? asString(sc.getAfter()) : asString(sc.getBefore());
                                break;
                            }
                        }
                        if (afterNs == null) {
                            // Fallback to current version
                            afterNs = String.join(".", view.currentVersion().defaultNamespace().levels());
                        }
                        replace.withDefaultNamespace(Namespace.of(afterNs.split("\\.")));

                        // Apply default catalog
                        for (StateChange sc : stateChanges) {
                            if ("defaultCatalog".equals(sc.getName())) {
                                String afterCat = sc.getOp() != Operation.NONE
                                    ? asString(sc.getAfter()) : asString(sc.getBefore());
                                if (afterCat != null) {
                                    replace.withDefaultCatalog(afterCat);
                                }
                                break;
                            }
                        }

                        replace.commit();
                    }

                    // Handle property changes independently
                    boolean hasPropertyChanges = stateChanges.stream()
                        .anyMatch(sc -> sc.getName().startsWith(PROPERTY_PREFIX) && sc.getOp() != Operation.NONE);

                    if (hasPropertyChanges) {
                        UpdateViewProperties propsUpdate = view.updateProperties();
                        for (StateChange sc : stateChanges) {
                            if (!sc.getName().startsWith(PROPERTY_PREFIX)) {
                                continue;
                            }
                            String key = sc.getName().substring(PROPERTY_PREFIX.length());
                            if (sc.getOp() == Operation.DELETE) {
                                propsUpdate.remove(key);
                            } else if (sc.getOp() == Operation.CREATE || sc.getOp() == Operation.UPDATE) {
                                if (sc.getAfter() != null) {
                                    propsUpdate.set(key, String.valueOf(sc.getAfter()));
                                }
                            }
                        }
                        propsUpdate.commit();
                    }

                    return ChangeMetadata.empty();
                });
                return new ChangeResponse(change, future);
            })
            .collect(Collectors.toList());
    }

    @NotNull
    private static TableIdentifier parseIdentifier(@NotNull final String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return TableIdentifier.of(Namespace.empty(), name);
        }
        String namespacePart = name.substring(0, lastDot);
        String viewName = name.substring(lastDot + 1);
        return TableIdentifier.of(Namespace.of(namespacePart.split("\\.")), viewName);
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
