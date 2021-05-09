package io.streamthoughts.kafka.specs.command.topic.subcommands.internal;


import io.streamthoughts.kafka.specs.resources.TopicResource;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public class TopicCandidates {

    private final Collection<TopicResource> topicsToAlter = new LinkedList<>();
    private final Collection<TopicResource> topicsToCreate = new LinkedList<>();
    private final Collection<TopicResource> topicsSynchronized = new LinkedList<>();
    private final Collection<TopicResource> topicsExistingOnCluster;

    public TopicCandidates(final Collection<TopicResource> declaredTopics,
                           final Map<String, TopicResource> existingTopics) {
        declaredTopics.forEach(topic -> {
            TopicResource clusterTopic = existingTopics.remove(topic.name());
            if (clusterTopic == null) {
                topicsToCreate.add(topic);
            } else if (topic.containsConfigsChanges(clusterTopic)) {
                topicsToAlter.add(topic.dropDefaultConfigs(clusterTopic));
            } else {
                topicsSynchronized.add(topic);
            }
        });
        // remaining remote topics are unknown and may be deleted
        this.topicsExistingOnCluster = existingTopics.values();
    }

    /**
     * @return the list of Topics that should be altered on cluster.
     */
    public Collection<TopicResource> topicsToAlter() {
        return Collections.unmodifiableCollection(topicsToAlter);
    }

    /**
     * @return the list of Topics that should be created on cluster.
     */
    public Collection<TopicResource> topicsToCreate() {
        return Collections.unmodifiableCollection(topicsToCreate);
    }

    /**
     * @return the list of Topics that are already synchronized on the cluster.
     */
    public Collection<TopicResource> topicsSynchronized() {
        return Collections.unmodifiableCollection(topicsSynchronized);
    }

    /**
     * @return the list of Topics that only exist on the cluster.
     */
    public Collection<TopicResource> topicsExistingOnlyOnCluster() {
        return Collections.unmodifiableCollection(topicsExistingOnCluster);
    }
}

