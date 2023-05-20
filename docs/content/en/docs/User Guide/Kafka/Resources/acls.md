---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Authorizations"
linkTitle: "Authorizations"
weight: 20
description: >
  Learn how to manage Kafka Authorizations and ACLs. 
---

{{% pageinfo color="info" %}}
KafkaPrincipalAuthorization resources are used to define Access Control Lists (ACLs) for principals authenticated
to your Kafka Cluster.
{{% /pageinfo %}}

Jikkou can be used to describe all ACL policies that need to be created on Kafka Cluster

## `KafkaPrincipalAuthorization`

### Specification

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Alice"
spec:
  roles: [ ]                     # List of roles to be added to the principal (optional)
  acls:                          # List of KafkaPrincipalACL (required)
    - resource:
        type: <The type of the resource>                            #  (required)
        pattern: <The pattern to be used for matching resources>    #  (required) 
        patternType: <The pattern type>                             #  (required) 
      type: <The type of this ACL>     # ALLOW or DENY (required) 
      operations: [ ]                  # Operation that will be allowed or denied (required) 
      host: <HOST>                     # IP address from which principal will have access or will be denied (optional)
```

For more information on how to define authorization and ACLs, see the official Apache Kafka
documentation: [Security](https://kafka.apache.org/documentation/#security_authz)

### Operations

The list below describes the valid values for the `spec.acls.[].operations` property :

* `READ`
* `WRITE`
* `CERATE`
* `DELETE`
* `ALTER`
* `DESCRIBE`
* `CLUSTER_ACTION`
* `DESCRIBE_CONFIGS`
* `ALTER_CONFIGS`
* `IDEMPOTENT_WRITE`
* `CREATE_TOKEN`
* `DESCRIBE_TOKENS`
* `ALL`

For more information see official Apache Kafka
documentation: [Operations in Kafka](https://kafka.apache.org/documentation/#operations_in_kafka)

### Resource Types

The list below describes the valid values for the `spec.acls.[].resource.type` property :

* `TOPIC`
* `GROUP`
* `CLUSTER`
* `USER`
* `TRANSACTIONAL_ID`

For more information see official Apache Kafka
documentation: [Resources in Kafka](https://kafka.apache.org/documentation/#resources_in_kafka)

### Pattern Types

The list below describes the valid values for the `spec.acls.[].resource.patternType` property :

* `LITERAL`: Use to allow or denied a principal to have access to a specific resource name.
* `MATCH`:  Use to allow or denied a principal to have access to all resources matching the given regex.
* `PREFIXED`: Use to allow or denied a principal to have access to all resources having the given prefix.

### Example

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"    # The api version (required)
kind: "KafkaPrincipalAuthorization"      # The resource kind (required)
metadata:
  name: "User:Alice"
spec:
  acls:
    - resource:
        type: 'topic'
        pattern: 'my-topic-'
        patternType: 'PREFIXED'
      type: "ALLOW"
      operations: [ 'READ', 'WRITE' ]
      host: "*"
    - resource:
        type: 'topic'
        pattern: 'my-other-topic-.*'
        patternType: 'MATCH'
      type: 'ALLOW'
      operations: [ 'READ' ]
      host: "*"
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Bob"
spec:
  acls:
    - resource:
        type: 'topic'
        pattern: 'my-topic-'
        patternType: 'PREFIXED'
      type: 'ALLOW'
      operations: [ 'READ', 'WRITE' ]
      host: "*"
```

## `KafkaPrincipalRole`

### Specification

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"    # The api version (required)
kind: "KafkaPrincipalRole"               # The resource kind (required)
metadata:
  name: <Name of role>                   # The name of the role (required)  
spec:
acls: [ ]                                # A list of KafkaPrincipalACL (required)
```

### Example

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalRole"
metadata:
  name: "KafkaTopicPublicRead"
spec:
  acls:
    - type: "ALLOW"
      operations: [ 'READ' ]
      resource:
        type: 'topic'
        pattern: '/public-([.-])*/'
        patternType: 'MATCH'
      host: "*"
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalRole"
metadata:
  name: "KafkaTopicPublicWrite"
spec:
  acls:
    - type: "ALLOW"
      operations: [ 'WRITE' ]
      resource:
        type: 'topic'
        pattern: '/public-([.-])*/'
        patternType: 'MATCH'
      host: "*"
---

apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Alice"
spec:
  roles:
    - "KafkaTopicPublicRead"
    - "KafkaTopicPublicWrite"
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Bob"
spec:
  roles:
    - "KafkaTopicPublicRead"
```


