#!/bin/bash
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The original authors
#
# Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
#
set -e

JIKKOU_CONFIGURATION_PATH=${JIKKOU_CONFIGURATION_PATH:-"/app/resources/"}
if [ -n "${JIKKOU_CONFIGURATION}" ]; then
    echo "${JIKKOU_CONFIGURATION}" > "${JIKKOU_CONFIGURATION_PATH}/application.yaml"
    export MICRONAUT_CONFIG_FILES="${JIKKOU_CONFIGURATION_PATH}/application.yaml"
fi

java -cp @/app/jib-classpath-file @/app/jib-main-class-file