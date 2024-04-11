package com.worker.consumer;

import com.worker.dto.RequestDTO;
import com.worker.producer.WorkerProducer;
import com.worker.service.WorkerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class WorkerConsumer {
    private static final Logger logger = LogManager.getLogger(WorkerConsumer.class);
    private final WorkerService workerService;

    public WorkerConsumer(WorkerService workerService) {
        this.workerService = workerService;
    }

    @RabbitListener(queues = {"${rabbitmq.manager.request.queue.name}"})
    public void consume(RequestDTO requestDTO) {
        logger.info("Received message with request id: {}, part number {}", requestDTO.getRequestId(), requestDTO.getPartNumber());
        workerService.startCrack(requestDTO);
    }
}