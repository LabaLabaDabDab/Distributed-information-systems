package com.manager.consumer;

import com.manager.dto.CrackResponseDTO;
import com.manager.dto.StatusResponseDTO;
import com.manager.service.ManagerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class ManagerConsumer {
    private static final Logger logger = LogManager.getLogger(ManagerConsumer.class);
    private final ManagerService managerService;

    public ManagerConsumer(ManagerService managerService) {
        this.managerService = managerService;
    }

    @RabbitListener(queues = "${rabbitmq.worker.response.queue.name}")
    public void consume(CrackResponseDTO crackResponseDTO) {
        logger.info("Received answer from worker: {}", crackResponseDTO);
        managerService.processWorkerResponse(crackResponseDTO);
    }
}