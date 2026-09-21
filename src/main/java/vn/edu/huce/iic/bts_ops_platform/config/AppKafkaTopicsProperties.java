package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.kafka-topics")
public record AppKafkaTopicsProperties(
        String sanluongChanged,
        String hopDongDeleted,
        @DefaultValue("6") int defaultPartitions,
        @DefaultValue("1") short defaultReplicationFactor) {
}
