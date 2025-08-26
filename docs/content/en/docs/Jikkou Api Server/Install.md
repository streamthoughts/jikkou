---
title: "Install Jikkou API Server"
linkTitle: "Install"
weight: 1
description: >
  This guide shows how to install Jikkou API Server.
---

## Releases

The latest stable release of Jikkou API Server is available:

* As a Java binary distribution (.zip) from [GitHub Releases](https://github.com/streamthoughts/jikkou/releases)
* As a docker image available from [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou-api-server).

### Standalone Installation

Follow these few steps to download the latest stable versions and get started.

#### Prerequisites

To be able to run Jikkou API Server, the only requirement is to have a working Java 21 installation. 
You can check the correct installation of Java by issuing the following command:

```bash
java -version
```

#### Step 1: Download

Download the latest Java binary distribution from the [GitHub Releases](https://github.com/streamthoughts/jikkou/releases) (e.g. `jikkou-api-server-0.36.0.zip`)

Unpack the download distribution and move the unpacked directory to a desired destination

```bash
unzip jikkou-api-server-$LATEST_VERSION.zip
mv jikkou-api-server-$LATEST_VERSION /opt/jikkou
```

#### Step 2: Start the API Server

Launch the application with:

```bash
./bin/jikkou-api-server.sh
```

#### Step 3: Test the API Server 

```bash
$ curl -sX GET http://localhost:28082 -H "Accept: application/json" | jq

{
  "version": "0.31.0",
  "build_time": "2023-11-14T18:07:38+0000",
  "commit_id": "dae1be11c092256f36c18c8f1d90f16b0c951716",
  "_links": {
    "self": {
      "href": "/",
      "templated": false
    },
    "get-apis": {
      "href": "/apis",
      "templated": false
    }
  }
}
```

### Step 4: Stop the API Server

```bash
PID=`ps -ef | grep -v grep | grep JikkouApiServer | awk '{print $2}'`
kill $PID
```

### Docker

```bash
# Run Docker
docker run -it \
--net host \
streamthoughts/jikkou-api-server:latest
```

## Development Builds

In addition to releases you can download or install development snapshots of Jikkou API Server.

### From Docker Hub

Docker images are built and push to [Docker Hub](https://hub.docker.com/r/streamthoughts/jikkou) from the latest `main`
branch.

They are not official releases, and may not be stable.
However, they offer the opportunity to test the cutting edge features.

```bash
$ docker run -it streamthoughts/jikkou-api-server:main
```