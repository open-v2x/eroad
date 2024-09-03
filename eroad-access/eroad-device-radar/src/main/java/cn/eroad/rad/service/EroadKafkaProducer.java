package cn.eroad.rad.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EroadKafkaProducer {

    private static KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public EroadKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        EroadKafkaProducer.kafkaTemplate = kafkaTemplate;
    }

    public static void send(String message, String topic) {
        kafkaTemplate.send(topic, message);
    }
}
