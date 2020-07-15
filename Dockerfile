# Copyright 2020 StreamThoughts.
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
FROM alpine:latest

ARG kafkaSpecsVersion

ENV KAFKA_SPECS_VERSION="${kafkaSpecsVersion}"

RUN apk update && apk --no-cache add unzip

COPY ./build/distributions/kafka-specs-${KAFKA_SPECS_VERSION}.zip kafka-specs-${KAFKA_SPECS_VERSION}.zip
RUN unzip kafka-specs-${KAFKA_SPECS_VERSION}.zip && mv kafka-specs-${KAFKA_SPECS_VERSION} kafka-specs

FROM azul/zulu-openjdk:13

ARG kafkaSpecsVersion
ARG kafkaSpecsBranch
ARG kafkaSpecsCommit

ENV KAFKA_SPECS_VERSION="${kafkaSpecsVersion}" \
    KAFKA_SPECS_COMMIT="${kafkaSpecsCommit}" \
    KAFKA_SPECS_BRANCH="${kafkaSpecsBranch}"

WORKDIR /opt/kafka-specs

COPY --from=0 /kafka-specs .

LABEL io.streamthoughts.docker.name="kafka-specs" \
      io.streamthoughts.docker.version=$KAFKA_SPECS_VERSION \
      io.streamthoughts.docker.branch=$KAFKA_SPECS_BRANCH \
      io.streamthoughts.docker.commit=$KAFKA_SPECS_COMMIT

ENTRYPOINT ["/bin/bash","/opt/kafka-specs/bin/kafka-specs"]


