apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaTopicList'
spec:
  topics:
  {% for location in values.locations %}
    - name: "{{ values.topic_prefix}}-iot-events-{{ location }}"
      partitions: {{ values.topic_configs.partitions }}
      replication_factor: {{ values.topic_configs.replication_factor }}
      config_map_refs:
        - TopicConfig
  {% endfor %}
---
apiVersion: "kafka.jikkou.io/v1beta2"
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
