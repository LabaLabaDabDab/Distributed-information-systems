package com.manager.controller;

import com.manager.dto.CrackResponseDTO;
import com.manager.dto.HashDTO;
import com.manager.dto.StatusRequestDTO;
import com.manager.dto.StatusResponseDTO;
import com.manager.exception.NoSuchTask;
import com.manager.exception.NotMD5Hash;
import com.manager.service.ManagerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hash/")
public class ManagerController {

    private static final Logger logger = LogManager.getLogger(ManagerController.class);

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/crack")
    public ResponseEntity<String> crackHash(@RequestBody HashDTO hashDTO) {
        try {
            logger.info("Received request for following hash crack: {}", hashDTO.getHash());
            return ResponseEntity.ok(managerService.crackHash(hashDTO));
        }
        catch (NotMD5Hash | IllegalArgumentException e) {
            logger.warn("Given hash is not in MD-5 format or maxLength is not valid");
            logger.warn(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing crack request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/response")
    public ResponseEntity<String> receiveResponse(@RequestBody CrackResponseDTO crackResponseDTO) {
        try {
            logger.info("Received response for requestId: {}", crackResponseDTO.getRequestId());
            managerService.processWorkerResponse(crackResponseDTO);
            return ResponseEntity.ok("Response received successfully");
        } catch (Exception e) {
            logger.error("Error processing response", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponseDTO> getTaskStatus(@RequestBody StatusRequestDTO statusRequestDTO) {
        try {
            logger.info("Checking status for requestId: {}", statusRequestDTO.getRequestId());
            return ResponseEntity.ok(managerService.checkStatus(statusRequestDTO.getRequestId()));
        }
        catch (NoSuchTask e) {
            logger.warn("No task associated with this UUID: {}", statusRequestDTO.getRequestId());
            logger.warn(e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error checking status for requestId: {}", statusRequestDTO.getRequestId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}