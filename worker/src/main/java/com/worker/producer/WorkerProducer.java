package com.worker.producer;

import com.worker.dto.ResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class WorkerProducer {
    private static final Logger logger = LogManager.getLogger(WorkerProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.worker.response.routing.key}")
    private String routingKey;

    public void sendMessage(ResponseDTO responseDTO) {
        logger.info("Starts sending the response");
        rabbitTemplate.convertAndSend(exchangeName, routingKey, responseDTO);
        logger.info("Sent answer queue for manager, request id: {}, part number: {}",
                responseDTO.getRequestId(), responseDTO.getPartNumber());
    }
}
