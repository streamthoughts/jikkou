---
categories: []
tags: ["feature", "resources"] 
title: "ACLs"
linkTitle: "ACLs"
weight: 20
description: >
  Learn how to describe ACL policies.
---

Jikkou can be used to describe all ACL policies that need to be created on Kafka Cluster

## The Resource Definition File

The _resource definition file_ for defining `acls` contains the following fields:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2" # The api version (required)
kind: KafkaAuthorizationList # The resource kind (required)
metadata: # (optional)
  labels: {}
  annotations: {}
spec:
  security:
    roles: # A list of the roles that can be assigned to users (optional)
    - name:  The name of the role
      permissions:  # A list of the permissions to add to the role
        - resource: # A list of the resources to add to the role
            type: The type of the resources, i.e., topic
            pattern: The pattern to be used for matching resources
            pattern_type: The type of the pattern
          allow_operations: [] # A list of the operations allowed for this role

    users: # A list of the users to manage
      - principal: The principal of the user
        roles: [] # A list of the role to add to the user (optional)
        permissions:  # A list of the permissions to add to the user (optional)
          - resource: # A list of the resources to add to the user
              type: The type of the resources, i.e., topic
              pattern: The pattern to be used for matching resources
              pattern_type: The type of the pattern
            allow_operations: [] # A list of the operations allowed for this user
```

## Usage

```bash
$ jikkou acls -h
```

```bash
Usage:

Apply the ACLs changes described by your specs-file against the Kafka cluster you are currently pointing at.

jikkou acls [-hV] [COMMAND]

Description:

This command can be used to create ACLs on a remote Kafka cluster

Options:

  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:

  apply     Apply all changes to the Kafka ACLs.
  create    Create the ACL policies missing on the cluster as describe in the specification file.
  delete    Delete all ACL policies not described in the specification file.
  describe  Describe all the ACLs that currently exist on remote cluster.
  help      Displays help information about the specified command
```  

## Examples

**Resource Specification File**

Create a file named `kafka-security.yaml` with the following content:

```yaml
apiVersion: 1
spec:
  security:
    roles:
      - name: 'AdminTopics'
        permissions:
          - resource:
              type: 'topic'
              pattern: '*'
              pattern_type: 'LITERAL'
            allow_operations: ['ALL:*']

      - name: 'AdminGroups'
        permissions:
          - resource:
              type: 'group'
              pattern: '*'
              pattern_type: 'LITERAL'
            allow_operations: ['ALL:*']

    users:
      - principal: 'User:admin'
        roles: [ 'AdminTopics', 'AdminGroups' ]

      - principal: 'User:admin-topics'
        roles: [ 'AdminTopics' ]
```

In the above file, we define multiple *roles* to apply to our _principals_. Jikkou will take care of creating all corresponding ACLs policies.

**Command**

Run the following command to create and/or update the ACLs declared in the resource file.

```bash
$ jikkou --bootstrap-servers localhost:9092 acls apply --files kafka-security.yml
```

**Output**

```
TASK [CREATE] Create a new ACL (ALLOW User:admin-user to ALL TOPIC:LITERAL:*) - CHANGED *****************
{
  "changed" : true,
  "end" : 1633980549689,
  "resource" : {
    "operation" : "ADD",
    "principal_type" : "User",
    "principal_name" : "admin-user",
    "resource_pattern" : "*",
    "pattern_type" : "LITERAL",
    "resource_type" : "TOPIC",
    "operation" : "ALL",
    "permission" : "ALLOW",
    "host" : "*",
    "name" : "admin-user",
    "principal" : "User:admin-user"
  },
  "failed" : false,
  "status" : "CHANGED"
}
TASK [CREATE] Create a new ACL (ALLOW User:kafka-user to ALL GROUP:LITERAL:*) - CHANGED *****************
{
  "changed" : true,
  "end" : 1633980549689,
  "resource" : {
    "operation" : "ADD",
    "principal_type" : "User",
    "principal_name" : "kafka-user",
    "resource_pattern" : "*",
    "pattern_type" : "LITERAL",
    "resource_type" : "GROUP",
    "operation" : "ALL",
    "permission" : "ALLOW",
    "host" : "*",
    "name" : "kafka-user",
    "principal" : "User:kafka-user"
  },
  "failed" : false,
  "status" : "CHANGED"
}
TASK [CREATE] Create a new ACL (ALLOW User:kafka-user to ALL TOPIC:LITERAL:*) - CHANGED *****************
{
  "changed" : true,
  "end" : 1633980549689,
  "resource" : {
    "operation" : "ADD",
    "principal_type" : "User",
    "principal_name" : "kafka-user",
    "resource_pattern" : "*",
    "pattern_type" : "LITERAL",
    "resource_type" : "TOPIC",
    "operation" : "ALL",
    "permission" : "ALLOW",
    "host" : "*",
    "name" : "kafka-user",
    "principal" : "User:kafka-user"
  },
  "failed" : false,
  "status" : "CHANGED"
}
EXECUTION in 2s 146ms
ok : 0, created : 3, altered : 0, deleted : 0 failed : 0
```