---
categories: []
tags: ["feature", "resources"]
title: "Kafka Resource Manager"
linkTitle: "Kafka Resource Manager"
weight: 70
description: >
    Learn how to configure custom manager for kafka resources
---

## Principal

Internally, the Jikkou's API defined various `KafkaResourceManager` sub-interfaces for managing each type of Kafka resources

* [`KafkaTopicManager`](https://github.com/streamthoughts/jikkou/tree/master/src/main/java/io/streamthoughts/jikkou/api/manager/KafkaTopicManager.java)
* [`KafkaBrokerManager`](https://github.com/streamthoughts/jikkou/tree/master/src/main/java/io/streamthoughts/jikkou/api/manager/KafkaBrokerManager.java)
* [`KafkaAclsManager`](https://github.com/streamthoughts/jikkou/tree/master/src/main/java/io/streamthoughts/jikkou/api/manager/KafkaAclsManager.java)
* [`KafkaQuotaManager`](https://github.com/streamthoughts/jikkou/tree/master/src/main/java/io/streamthoughts/jikkou/api/manager/KafkaQuotaManager.java)

Currently, Jikkou provides built-in implementations for each of these interface based on the Kafka's AdminClient.
Thus, depending on the Kafka service manager you use, you may need to implement custom managers for supporting more features.

Finally, the implementations to be used the Jikkou can be configured directly in your `application.conf` file.


```hocon
jikkou {
    # The KafkaResourceManager classes and configurations used for managing kafka resources
    managers {
        kafka {
            brokers {
                type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaBrokerManager
                config = {}
            }
            topics {
                type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaTopicManager
                config = {}
            }
            acls {
                type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaAclsManager
                config = {}
            }
            quotas {
                type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaQuotasManager
                config = {}
            }
        }
    }
}
```