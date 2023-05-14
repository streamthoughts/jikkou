apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaTopicList'
items:
{% for location in values.locations %}
- metadata:
    name: "{{ values.topic_prefix}}-iot-events-{{ location }}"
  spec:
    partitions: {{ values.topic_configs.partitions }}
    replicas: {{ values.topic_configs.replication_factor }}
    configMapRefs:
    - TopicConfig
{% endfor %}
---
apiVersion: "core.jikkou.io/v1beta2"
kind: "ConfigMap"
metadata:
  name: TopicConfig
template:
  values:
    default_min_insync_replicas: "{{ system.env.DEFAULT_REPLICATION_FACTOR | default(3, true) | int | add(-1) }}"
data:
  retention.ms: 3600000
  max.message.bytes: 20971520
  min.insync.replicas: '{% raw %}{{ values.default_min_insync_replicas }}{% endraw %}'
