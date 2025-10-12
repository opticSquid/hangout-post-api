package com.hangout.core.post_api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.hangout.core.post_api.dto.event.HeartEvent;

@Configuration
public class BatchKafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBrokers;
    @Value("${spring.application.name}")
    private String consumerGroupId;
    @Value("${hangout.kafka.heart.maxPollRecords}")
    private Integer maxPollRecords;
    @Value("${hangout.kafka.heart.fetchMinBytes}")
    private Integer fetchMinBytes;
    @Value("${hangout.kafka.heart.fetchMaxWaitMs}")
    private Integer fetchMaxWaitMs;

    @Bean
    ConcurrentKafkaListenerContainerFactory<UUID, HeartEvent> batchEventContainerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId); // Custom group ID
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.hangout.core.post_api.dto.event");
        // Custom configurations for like events
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // Fetch up to 100 records
        consumerProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytes); // Fetch at least 1 KB of data
        consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs); // Wait up to 500ms for more data

        ConsumerFactory<UUID, HeartEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        ConcurrentKafkaListenerContainerFactory<UUID, HeartEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true); // Enable batch processing
        // Manual acknowledgment mode (important!)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}