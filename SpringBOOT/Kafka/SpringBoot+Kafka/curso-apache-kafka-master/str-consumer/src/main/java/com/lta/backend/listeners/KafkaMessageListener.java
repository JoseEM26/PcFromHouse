package com.lta.backend.listeners;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class KafkaMessageListener {

    @KafkaListener(topics = "mi-topic", groupId = "group-demo", containerFactory = "validMessageContainerFactory")
    public void listen(String message) {
        log.info("Mensaje recibido: {}", message);
    }
}
