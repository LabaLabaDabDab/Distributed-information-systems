package com.worker.service;

import com.worker.dto.RequestDTO;
import com.worker.dto.ResponseDTO;
import com.worker.producer.WorkerProducer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkerService {
    private static final Logger logger = LogManager.getLogger(WorkerService.class);
    private final ExecutorService threadPool;

    @Autowired
    WorkerProducer workerProducer;

    public WorkerService(){
        this.threadPool = Executors.newFixedThreadPool(5);
    }

    public void startCrack(RequestDTO requestDTO) {
        threadPool.submit(() -> {
            try {
                var result = crackHash(requestDTO);
                workerProducer.sendMessage(result);
            } catch (Exception e) {
                logger.error("Error occurred during hash cracking: {}", e.getMessage());
            }
        });
    }

    public ResponseDTO crackHash(RequestDTO requestDTO) {
        logger.info("Processing crackHash request for requestId: {}", requestDTO.getRequestId());
        List<String> answers = new ArrayList<>();


        var partNumber = requestDTO.getPartNumber();
        var partAlphabet = requestDTO.getPartAlphabet();

        for (int i = 1; i <= requestDTO.getMaxLength(); i++) {
            Generator.permutation(requestDTO.getAlphabet())
                    .withRepetitions(i)
                    .stream()
                    .forEach(combinationArray -> {
                        for (String partChar : partAlphabet) {
                            var combination = String.join("", combinationArray) + partChar;
                            var hash = DigestUtils.md5Hex(combination);

                            if (hash.equals(requestDTO.getHash())) {
                                logger.info("For hash {} found word {}", requestDTO.getHash(), combination);
                                answers.add(combination);
                                break;
                            }
                        }
                    });
        }

        ResponseDTO response = new ResponseDTO();
        response.setRequestId(requestDTO.getRequestId());
        response.setPartNumber(partNumber);
        response.setAnswers(answers);

        return response;
    }
}