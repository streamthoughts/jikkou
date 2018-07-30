Kafka Specs (Cluster management made easy!)
==========================================

[![CircleCI](https://circleci.com/gh/Zenika/kafka-specs/tree/feature%2Fcircle-ci-init.svg?style=svg&circle-token=44dffabbf60d064f8195246fbc2a63549998e582)](https://circleci.com/gh/Zenika/kafka-specs/tree/feature%2Fcircle-ci-init)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/Zenika/kafka-specs/blob/master/LICENSE)

[Apache Kafka](http://kafka.apache.org/) is a high-throughput, distributed, publish-subscribe messaging system.

**KafkaSpecs** is a java tool to simplify the management of your Kafka Topics and ACLs.

## Requirements :

1. Kafka 1.0.0 ...
2. Java 8+

## Quick-Start

Kafka Topics are described using simple YAML description file : 

cluster-dev-topics.yaml : 
```yaml
topics:
- configs:
    cleanup.policy: compact
    compression.type: producer
    min.insync.replicas: '1'
  name: my-topic
  partitions: 12
  replication_factor: 1
```

## How to Manage Topics ?

**KafkaSpecs can be used to create topics :**

```bash
./bin/kafka-specs --create --bootstrap-server localhost:9092 --verbose --file cluster-dev-topics.yaml --entity-type topics
```

(output)
```json
TASK [CREATE] Create a new topic my-topic (partitions=12, replicas=1) - CHANGED *************************
{
  "changed": true,
  "end": 1539682759748,
  "resource": {
    "name": "my-topic",
    "partitions": 12,
    "replicationFactor": 1,
    "configs": {
      "cleanup.policy": "compact",
      "compression.type": "producer",
      "min.insync.replicas": "1"
    }
  },
  "failed": false,
  "status": "CHANGED"
}
ok : 0, changed : 1, failed : 0
```

**KafkaSpecs can be used describe existing topics:**

```bash
./bin/kafka-specs --export --bootstrap-server localhost:9092 --default-configs --entity-type topics
```
(output)
```json
topics:
- configs:
    cleanup.policy: compact
    compression.type: producer
    delete.retention.ms: '86400000'
    file.delete.delay.ms: '60000'
    flush.messages: '9223372036854775807'
    flush.ms: '9223372036854775807'
    follower.replication.throttled.replicas: ''
    index.interval.bytes: '4096'
    leader.replication.throttled.replicas: ''
    max.message.bytes: '1000012'
    message.format.version: 1.0-IV0
    message.timestamp.difference.max.ms: '9223372036854775807'
    message.timestamp.type: CreateTime
    min.cleanable.dirty.ratio: '0.5'
    min.compaction.lag.ms: '0'
    min.insync.replicas: '1'
    preallocate: 'false'
    retention.bytes: '-1'
    retention.ms: '604800000'
    segment.bytes: '1073741824'
    segment.index.bytes: '10485760'
    segment.jitter.ms: '0'
    segment.ms: '604800000'
    unclean.leader.election.enable: 'false'
  name: my-topic
  partitions: 12
  replicationFactor: 1
```

## How to Manage ACLs

**KafkaSpecs can be used to simply describe all ACLs that need to be created on Kafka Cluster:**

```yaml
acls:
  access_policies:
    - principal : 'User:benchmark'
      groups  : []
      permissions :
        - resource :
            type : 'topic'
            pattern : 'bench-'
            patternType : 'PREFIXED'
          allow_operations : ['READ:*', 'WRITE:*']
        - resource :
            type : 'group'
            pattern : '*'
            patternType : 'LITERAL'
          allow_operations : ['DESCRIBE:*']
```

You can also defined a *group_policies* to defined ACLs to be applied to multiple principal. 
Kafka Specs will take care of creating all corresponding ACLs

```yaml
acls:
  group_policies:
    - name : 'spec-access-all-topics'
      resource :
        type : 'topic'
        pattern : '*'
        patternType : 'LITERAL'
      allow_operations : ['ALL:*']

     - name : 'spec-access-all-groups'
      resource :
        type : 'group'
        pattern : '*'
        patternType : 'LITERAL'
      allow_operations : ['ALL:*']

  access_policies:
    - principal : 'User:kafka'
      groups    : [ 'spec-access-all-topics', 'spec-access-all-groups' ]
      
    - principal : 'User:admin-topic'
      groups    : [ 'spec-access-all-topics']
```

As of Kafka 2.0.0, you can use LITERAL and PREFIXED pattern-type to define new ACLs, then MATCH and ANY for filtering.

With Kafka Specs you can use the pattern-type MATCH to create ACLs. This will defined ACLs with LITERAL pattern type for all topics matching the defined regex.

```yaml
acls:
  access_policies:
    - principal : 'User:benchmark'
      groups  : []
      permissions :
        - resource :
            type : 'topic'
            pattern : '/bench-([.-])*/'
            patternType : 'MATCH'
          allow_operations : ['READ:*', 'WRITE:*']
```

```json
TASK [CREATE] Create a new ACL (ALLOW User:benchmark to WRITE TOPIC:LITERAL:bench-p1-r1) - CHANGED ******
{
  "changed": true,
  "end": 1539685171168,
  "resource": {
    "principalType": "User",
    "principalName": "benchmark",
    "resourcePattern": "bench-p1-r1",
    "patternType": "LITERAL",
    "resourceType": "TOPIC",
    "operation": "WRITE",
    "permission": "ALLOW",
    "host": "*"
  },
  "failed": false,
  "status": "CHANGED"
}
TASK [CREATE] Create a new ACL (ALLOW User:benchmark to READ TOPIC:LITERAL:bench-p1-r1) - CHANGED *******
{
  "changed": true,
  "end": 1539685171168,
  "resource": {
    "principalType": "User",
    "principalName": "benchmark",
    "resourcePattern": "bench-p1-r1",
    "patternType": "LITERAL",
    "resourceType": "TOPIC",
    "operation": "READ",
    "permission": "ALLOW",
    "host": "*"
  },
  "failed": false,
  "status": "CHANGED"
}

```

Limitation : Currently Kafka Specs only support create and describe actions.

## All Actions

```bash
./bin/kafka-specs        
                                                     
Create, Alter, Delete, Describe or clean Kafka cluster resources
Option                                  Description                           
------                                  -----------                           
--alter                                 OPTION : Alter all existing entities  
                                          that have configuration changed     
--bootstrap-server <String: server(s)   REQUIRED: The server to connect to.   
  to use for bootstrapping>                                                   
--clean-all                             COMMAND: Temporally set retention.ms  
                                          to 0 in order to delete messages for
                                          each topic                          
--command-property <String: command     A property file containing configs to 
  config property>                        be passed to Admin Client.          
--command.config <File: command config  A property file containing configs to 
  property file>                          be passed to Admin Client.          
--create                                OPTION : Create all entities that     
                                          currently do not exist on remote    
                                          cluster                             
--default-configs                       OPTION : Export built-in default      
                                          configuration for configs that have 
                                          a default value                     
--delete                                OPTION : Delete all remote entities   
                                          which are not described in          
                                          specifications                      
--describe                              COMMAND: Describe resources           
                                          configuration of a specified cluster
--diff                                  COMMAND: Display difference between   
                                          cluster resources and the specified 
                                          specifications                      
--dry-run                               OPTION : Execute command in Dry-Run   
                                          mode                                
--entity-type <String>                  OPTION : entity on which to execute   
                                          command [topics|users]              
--execute                               COMMAND: Align cluster resources with 
                                          the specified specifications        
--file <File>                           The cluster specification to used for 
                                          the command.                        
--help                                  Print usage information.              
--topics <String>                       OPTION : Only run command for this of 
                                          topics (separated by ,)             
--verbose                               Print resources details
``` 

## How to build project ?

You need to have [Gradle](http://www.gradle.org/installation) and [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed.

### To build jar
```bash
./gradlew jar
```
### To package distribution
```bash
./gradlew distTar
```
### Build javadoc
```
./gradlew javadoc
```

### Cleaning build
```
./gradlew clean
```

## Contributions
Any contribution is welcome

## Licence
Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
