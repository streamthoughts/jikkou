apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaTopicList'
items:
{% for country in values.countryCodes %}
- metadata:
    name: "{{ values.topicPrefix}}-iot-events-{{ country }}"
  spec:
    partitions: {{ values.topicConfigs.partitions }}
    replicas: {{ values.topicConfigs.replicas }}
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
    default_min_insync_replicas: "{{ values.topicConfigs.replicas | default(3, true) | int | add(-1) }}"
data:
  retention.ms: 3600000
  max.message.bytes: 20971520
  min.insync.replicas: '{% raw %}{{ values.default_min_insync_replicas }}{% endraw %}'