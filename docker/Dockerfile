#
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The original authors
#
# Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
#
FROM alpine:3.18.3

LABEL org.opencontainers.image.title=jikkou \
      org.opencontainers.image.licenses=Apache-2.0 \
      org.opencontainers.image.url=https://streamthoughts.github.io/jikkou/ \
      org.opencontainers.image.documentation=https://streamthoughts.github.io/jikkou/ \
      org.opencontainers.image.source=https://github.com/streamthoughts/jikkou

ARG VERSION
ARG CREATED
ARG COMMIT

ENV USER_ID=10000 \
    GROUP_ID=10001 \
    USER_NAME="appuser" \
    GROUP_NAME="appuser" \
    JIKKOUCONFIG="/etc/jikkou/config"

RUN mkdir -p /etc/jikkou/

WORKDIR /app

RUN addgroup -g $GROUP_ID $GROUP_NAME && \
    adduser --shell /sbin/nologin --disabled-password \
    --no-create-home --uid $USER_ID --ingroup $GROUP_NAME $USER_NAME

USER $USER_NAME

COPY --chmod=0755 ./jikkou-cli-${VERSION}-runner ./jikkou

LABEL org.opencontainers.image.created=$CREATED \
      org.opencontainers.image.version=$VERSION \
      org.opencontainers.image.revision=$COMMIT

ENV PATH="$PATH:/app"

ENTRYPOINT ["./jikkou"]


