package com.worker.controller;

import com.worker.dto.RequestDTO;
import com.worker.dto.ResponseDTO;
import com.worker.dto.StatusWorkDTO;
import com.worker.service.WorkerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/worker/hash/crack")
public class WorkerController {
    private static final Logger logger = LogManager.getLogger(WorkerController.class);

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping("/task")
    public ResponseEntity<StatusWorkDTO> crackHash(@RequestBody RequestDTO requestDTO) {
        try {
            logger.info("Received crack task request for requestId: {}", requestDTO.getRequestId());

            StatusWorkDTO statusWorkDTO = workerService.startCracking(requestDTO);


            logger.info("Sending work status for requestId: {}", requestDTO.getRequestId());

            return ResponseEntity.ok(statusWorkDTO);
        } catch (Exception e) {
            logger.error("Error processing crack task request for requestId: {}", requestDTO.getRequestId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}