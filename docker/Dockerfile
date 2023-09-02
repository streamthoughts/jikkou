# Copyright 2023 The original authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
FROM alpine:3.18.3

ARG jikkouVersion
ARG jikkouBranch
ARG jikkouCommit

ENV JIKKOU_VERSION="${jikkouVersion}" \
    JIKKOU_COMMIT="${jikkouCommit}" \
    JIKKOU_BRANCH="${jikkouBranch}"

WORKDIR /opt/jikkou

COPY ./jikkou-cli-${JIKKOU_VERSION}-runner jikkou

LABEL io.streamthoughts.docker.name="jikkou" \
      io.streamthoughts.docker.version=$JIKKOU_VERSION \
      io.streamthoughts.docker.branch=$JIKKOU_BRANCH \
      io.streamthoughts.docker.commit=$JIKKOU_COMMIT

ENTRYPOINT ["/bin/bash","/opt/jikkou/bin/jikkou"]


