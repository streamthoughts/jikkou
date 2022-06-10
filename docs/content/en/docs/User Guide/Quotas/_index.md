---
categories: []
tags: ["feature", "resources"] 
title: "Quotas"
linkTitle: "Quotas"
weight: 30
description: >
  Learn how to define quotas for consumers and/or producers.
---


Jikkou allows defining the quotas to apply to consumers and/or producers identified by a `client-id` or a user `principal`.

## The Resource Specification File

The _resource specification file_ for defining `quotas` contains the following fields:

```yaml
apiVersion: 1 # The jikkou API version (required)
spec:
  quotas:
  - type: The quota type (required)
    entity:
      client_id: The id of the client (required depending on the quota type).
      user:  The principal of the user (required depending on the quota type).
    configs:
      request_byte_rate: The quota in percentage (%) of total requests (optional)
      producer_byte_rate: The quota in bytes for restricting data production (optional)
      consumer_byte_rate: The quota in bytes for restricting data consumption (optional)
```

**The list below describes the supported quota types:**

* `USERS_DEFAULT`: Set default quotas for all users.
* `USER`: Set quotas for a specific user principal.
* `USER_CLIENT`: Set quotas for a specific user principal and a specific client-id.
* `USER_ALL_CLIENTS`: Set default quotas for a specific user and all clients.
* `CLIENT`: Set default quotas for a specific client.
* `CLIENTS_DEFAULT`: Set default quotas for all clients.

## Usage

```bash
$ jikkou quotas -h
```

```bash
Apply the quotas changes described by your specs-file against the Kafka cluster you are currently pointing at.

jikkou quotas [-hV] [COMMAND]

Description:

This command can be used to create, alter, delete or describe quotas on a remote Kafka cluster

Options:

  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:

  alter     Update the client quotas on the cluster as describe in the specification file.
  apply     Apply all changes to the Kafka client quotas.
  create    Create the client quotas missing on the cluster as describe in the specification file.
  delete    Delete all client-quotas not described in the specification file.
  describe  Describe quotas that currently exist on the remote Kafka cluster.
  help      Displays help information about the specified command
```