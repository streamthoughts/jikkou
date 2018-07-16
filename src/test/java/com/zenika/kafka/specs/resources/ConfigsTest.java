package com.zenika.kafka.specs.resources;

import org.apache.kafka.common.config.TopicConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigsTest {

    private static final String DELETE_CLEANUP_POLICY = "delete";
    private static final String COMPACT_CLEANUP_POLICY = "compact";

    private Configs defaultTopicConfigs;


    @Before
    public void setUp() {
        defaultTopicConfigs = new Configs();
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1",  true));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG, "false",  true));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, DELETE_CLEANUP_POLICY,  true));
    }

    @Test
    public void shouldDetectNoChangesGivenEmptyConfigsWhenComparingWithDefaultValues() {
        Configs emptyConfigs = Configs.emptyConfigs();
        boolean result = emptyConfigs.containsChanges(defaultTopicConfigs);
        Assert.assertFalse(result);
    }

    @Test
    public void shouldDetectNoChangesGivenOverrideConfigsWithNoChangesWhenComparingWithDefaultValues() {
        Configs configs = new Configs();
        configs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, DELETE_CLEANUP_POLICY));

        boolean result = configs.containsChanges(defaultTopicConfigs);
        Assert.assertFalse(result);
    }

    @Test
    public void shouldDetectChangesGivenEmptyConfigsWhenComparingWithDefaultAndOverrideValues() {
        // override default value
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, COMPACT_CLEANUP_POLICY));

        Configs emptyConfigs = Configs.emptyConfigs();
        boolean result = emptyConfigs.containsChanges(defaultTopicConfigs);
        Assert.assertTrue(result);
    }


    @Test
    public void shouldDetectChangesGivenOverrideConfigsWhenComparingWithDefaultValues() {
        Configs configs = new Configs();
        configs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, COMPACT_CLEANUP_POLICY));

        boolean result = configs.containsChanges(defaultTopicConfigs);
        Assert.assertTrue(result);
    }

    @Test
    public void shouldBeAbleToGetSubDefaultDefaultConfigs() {
        // override default value
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, COMPACT_CLEANUP_POLICY));
        Configs configs = defaultTopicConfigs.defaultConfigs();

        Assert.assertEquals(2, configs.size());
        Assert.assertNotNull(configs.get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));
        Assert.assertNotNull(configs.get(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG));
    }

    @Test
    public void shouldFilterOnNonEqualsConfigs() {
        Configs configs = new Configs(defaultTopicConfigs.values());
        // override default config entry
        configs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, COMPACT_CLEANUP_POLICY));
        // add a new config entry
        configs.add(new ConfigValue(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "10000"));

        Assert.assertEquals(4, configs.size());
        Configs result = configs.filters(defaultTopicConfigs);

        Assert.assertEquals(2, result.size());
        Assert.assertNotNull(configs.get(TopicConfig.MAX_MESSAGE_BYTES_CONFIG));
        Assert.assertNotNull(configs.get(TopicConfig.CLEANUP_POLICY_COMPACT));
    }

}