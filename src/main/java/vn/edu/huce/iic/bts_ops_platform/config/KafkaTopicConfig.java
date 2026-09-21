package vn.edu.huce.iic.bts_ops_platform.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final AppKafkaTopicsProperties topicsProperties;

    /**
     * Partitioned by hopDongId (message key) so all changes for the same
     * hợp đồng are processed in order by a single consumer. Uses the shared
     * default partitions/replicas below — pass explicit values to
     * {@link #topic(String, int, short)} only when a topic needs to diverge.
     */
    @Bean
    public NewTopic sanLuongChangedTopic() {
        return topic(topicsProperties.sanluongChanged());
    }

    @Bean
    public NewTopic hopDongDeletedTopic() {
        return topic(topicsProperties.hopDongDeleted());
    }

    private NewTopic topic(String name) {
        return topic(name, topicsProperties.defaultPartitions(), topicsProperties.defaultReplicationFactor());
    }

    private NewTopic topic(String name, int partitions, short replicationFactor) {
        return TopicBuilder.name(name).partitions(partitions).replicas(replicationFactor).build();
    }

    /**
     * Built manually (rather than relying on Spring Boot's Kafka autoconfiguration)
     * because this project's Boot version doesn't pull in spring-boot-autoconfigure's
     * Kafka module, so no ProducerFactory/KafkaTemplate bean exists by default.
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
