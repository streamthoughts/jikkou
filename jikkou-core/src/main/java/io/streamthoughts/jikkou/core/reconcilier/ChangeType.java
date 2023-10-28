/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.reconcilier;

/**
 * Represents the supported type of changes.
 */
public enum ChangeType {

    /**
     * the modification neither creates new resource objects nor updates existing ones.
     */
    NONE,
    /**
     * the change creates a new resource object.
     */
    ADD,
    /**
     * the change deletes an existing resource object.
     */
    DELETE,
    /**
     * the change updates an existing resource object.
     */
    UPDATE,
    /**
     * the change is not applicable and should be ignored or filtered - internal usage
     */
    IGNORE;
}