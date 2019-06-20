FROM openjdk:8-jdk
VOLUME /opt/kafka-specs

RUN apt-get update && \
	apt-get install -y --no-install-recommends unzip

COPY build/distributions/kafka-specs-0.1.0.zip kafka-specs-0.1.0.zip
RUN unzip kafka-specs-0.1.0.zip

ENTRYPOINT ["/bin/bash","kafka-specs-0.1.0/bin/kafka-specs"]