package com.learnkafka.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.learnkafka.service.LibraryEventsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableKafka
@Slf4j
@AllArgsConstructor
public class LibraryEventsConsumerConfig {

    private final LibraryEventsService libraryEventsService;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setErrorHandler(((thrownException, data) ->
                log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data)));
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback((context -> {
            if (context.getLastThrowable().getCause() instanceof RecoverableDataAccessException) {
                log.info("Inside the recoverable logic");
                Arrays.asList(context.attributeNames()).forEach(attribute -> {
                    log.info("Attribute name: {}", attribute);
                    log.info("Attribute value: {}", context.getAttribute(attribute));
                });
                final ConsumerRecord<Integer, String> record = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                libraryEventsService.handleRecovery(record);
            } else {
                log.info("Inside the non recoverable logic");
                throw new RuntimeException(context.getLastThrowable().getMessage());
            }
            return null;
        }));

        return factory;
    }

    private RetryTemplate retryTemplate() {
        var policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(1000);
        final RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(simpleRetryPolicy());
        template.setBackOffPolicy(policy);
        return template;
    }

    private RetryPolicy simpleRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> map = new HashMap<>();
        map.put(IllegalArgumentException.class, false);
        map.put(RecoverableDataAccessException.class, true);
        return new SimpleRetryPolicy(3, map, true);
    }

}