/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs;

import com.zenika.kafka.specs.command.OperationType;

/**
 * Simple interface to get a human-readable description of an executed operation.
 */
public interface Description {

    /**
     * Get the type of the operation.
     */
    OperationType operation();

    /**
     * Get a textual description of the operation.
     */
    String textDescription();


    interface Create extends Description {

        /**
         * @return {@link OperationType#CREATE}.
         */
        @Override
        default OperationType operation() {
            return OperationType.CREATE;
        }
    }

    interface Alter extends Description {

        /**
         * @return {@link OperationType#ALTER}.
         */
        @Override
        default OperationType operation() {
            return OperationType.ALTER;
        }
    }

    interface Delete extends Description {

        /**
         * @return {@link OperationType#DELETE}.
         */
        @Override
        default OperationType operation() {
            return OperationType.DELETE;
        }
    }

    interface Unknown extends Description {

        /**
         * @return {@link OperationType#UNKNOWN}.
         */
        @Override
        default OperationType operation() {
            return OperationType.UNKNOWN;
        }
    }

}
