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

ARG jikkouVersion

ENV JIKKOU_VERSION="${jikkouVersion}"

RUN apk update && apk --no-cache add unzip

COPY ./jikkou-${JIKKOU_VERSION}-runner.zip jikkou-${JIKKOU_VERSION}-runner.zip
RUN unzip jikkou-${JIKKOU_VERSION}-runner.zip && mv jikkou-${JIKKOU_VERSION}-runner jikkou

FROM azul/zulu-openjdk:17

ARG jikkouVersion
ARG jikkouBranch
ARG jikkouCommit

ENV JIKKOU_VERSION="${jikkouVersion}" \
    JIKKOU_COMMIT="${jikkouCommit}" \
    JIKKOU_BRANCH="${jikkouBranch}"

WORKDIR /opt/jikkou

COPY --from=0 /jikkou .

LABEL io.streamthoughts.docker.name="jikkou" \
      io.streamthoughts.docker.version=$JIKKOU_VERSION \
      io.streamthoughts.docker.branch=$JIKKOU_BRANCH \
      io.streamthoughts.docker.commit=$JIKKOU_COMMIT

ENTRYPOINT ["/bin/bash","/opt/jikkou/bin/jikkou"]


