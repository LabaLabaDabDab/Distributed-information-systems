package com.manager.producer;

import com.manager.dto.CrackRequestDTO;
import com.manager.exception.RabbitException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ManagerProducer {
    private static final Logger logger = LogManager.getLogger(ManagerProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.manager.request.routing.key}")
    private String routingKey;

    public void sendRequest(CrackRequestDTO crackRequestDTO) throws RabbitException {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, crackRequestDTO);
            logger.info("Sent message to RabbitMQ: id {}, part number: {}", crackRequestDTO.getRequestId(), crackRequestDTO.getPartNumber());
        }
        catch (AmqpException e) {
            logger.error("Unable to send message to RabbitMQ: id {}, part number {}", crackRequestDTO.getRequestId(), crackRequestDTO.getPartNumber());
            throw new RabbitException();
        }
    }

}
