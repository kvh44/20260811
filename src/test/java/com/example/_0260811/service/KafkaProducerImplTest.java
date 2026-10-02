package com.example._0260811.service;

import com.example._0260811.config.KafkaConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerImpl kafkaProducer;

    @Test
    void sendMessagePublishesToConfiguredTopic() {
        String message = "Test message";

        kafkaProducer.sendMessage(message);

        verify(kafkaTemplate).send(KafkaConfig.TOPIC20260930, message);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}
