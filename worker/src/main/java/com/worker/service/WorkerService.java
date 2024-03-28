package com.worker.service;

import com.worker.dto.RequestDTO;
import com.worker.dto.ResponseDTO;
import com.worker.dto.StatusWorkDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.paukov.combinatorics3.Generator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkerService {
    private static final Logger logger = LogManager.getLogger(WorkerService.class);

    private static final String MANAGER_CRACK_ENDPOINT = "http://manager:8080/api/hash/response";

    public StatusWorkDTO startCracking(RequestDTO requestDTO){
        logger.info("Processing crackHash request for requestId: {}", requestDTO.getRequestId());

        StatusWorkDTO statusWorkDTO = new StatusWorkDTO();
        statusWorkDTO.setStatusWork("working");
        Thread thread = new Thread(() -> crackHash(requestDTO));
        thread.start();

        return statusWorkDTO;
    }

    public void crackHash(RequestDTO requestDTO) {
        List<String> answers = new ArrayList<>();

        for (int i = 1; i <= requestDTO.getMaxLength(); i++) {
            Generator.permutation(requestDTO.getAlphabet())
                    .withRepetitions(i)
                    .stream()
                    .forEach(combinationArray -> {
                        String combination = String.join("", combinationArray);
                        String hash = DigestUtils.md5Hex(combination);

                        if (hash.equals(requestDTO.getHash())) {
                            logger.info("For hash {} found word {}", requestDTO.getHash(), combination);
                            answers.add(combination);
                        }
                    });
        }

        ResponseDTO response = new ResponseDTO();
        response.setRequestId(requestDTO.getRequestId());
        response.setAnswers(answers);

        WebClient client = WebClient.create(MANAGER_CRACK_ENDPOINT);
        try {
            client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            logger.info("Response sent successfully to manager");
        } catch (Exception e) {
            logger.error("Error sending response to manager", e);
        }
    }
}