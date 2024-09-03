package cn.eroad.rad.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    private static KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaProducer.kafkaTemplate = kafkaTemplate;
    }

    public static void send(String message, String topic) {
        log.info("start topic = {}, the length of message = {}", topic, message.length());
        kafkaTemplate.send(topic, message);
        log.info("end topic = {}, the length of message = {}", topic, message.length());
    }
}
