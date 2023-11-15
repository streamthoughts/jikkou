---
title: "Configurations"
linkTitle: "Configurations"
weight: 2
description: >
  Learn how to configure Jikkou API Server.
---

Jikkou API Server is built with [Micronaut Framework](https://micronaut.io/).

The default configuration file is located in the installation directory of you server under the
path `/etc/application.yaml`. 

You can either modify this configuration file directly or create a new one.
Then, your configuration file path can be targeted through the `MICRONAUT_CONFIG_FILES` environment variable.

A YAML Configuration file example can be found
here: [application.yaml](https://github.com/streamthoughts/jikkou/blob/main/jikkou-rest-api/jikkou-api-server/src/main/resources/application.yaml)

{{% alert title="Note" color="info" %}}
For more information about how to configure the application, we recommend you to read the official Micronaut documentation (see: [Application Configuration](https://docs.micronaut.io/latest/guide/#config)).
{{% /alert %}}