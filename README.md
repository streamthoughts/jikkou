Kafka Specs (Topics management made easy!)
==========================================

[Apache Kafka](http://kafka.apache.org/) is a high-throughput, distributed, publish-subscribe messaging system.

**KafkaSpecs** is a java tool to simplify the management of your Kafka topics.

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
  replicationFactor: 1
```

**KafkaSpecs can be used to create topics :**

```bash
./bin/kafka-specs --create --bootstrap-server localhost:9092 --verbose --file cluster-dev-topics.yaml
```

(output)
```
TASK [create] changed *****************************
{
  "changed": true,
  "cmd": "create",
  "end": 1531514308647,
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
  "failed": false
}
ok : 0, changed : 1, failed : 0
```

**KafkaSpecs can be used describe existing topics:**

```bash
./bin/kafka-specs --export --bootstrap-server localhost:9092 --default-configs
```
(output)
```
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

## All Actions

```bash
./bin/kafka-specs        
                                                     
Create, Alter, Delete, Describe or clean Kafka cluster resources
Option                                  Description                           
------                                  -----------                           
--alter                                 OPTION : Alter all existing topics    
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
--create                                OPTION : Create all topics that       
                                          currently do not exist on remote    
                                          cluster                             
--default-configs                       OPTION : Export built-in default      
                                          configuration for configs that have 
                                          a default value                     
--delete                                OPTION : Delete all remote topics     
                                          which are not described in          
                                          specifications                      
--describe                              COMMAND: Describe resources           
                                          configuration of a specified cluster
--diff                                  COMMAND: Display difference between   
                                          cluster resources and the specified 
                                          specifications                      
--dry-run                               OPTION : Execute command in Dry-Run   
                                          mode                                
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
