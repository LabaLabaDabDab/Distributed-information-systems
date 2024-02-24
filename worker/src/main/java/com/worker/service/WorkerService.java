package com.worker.service;

import com.worker.dto.RequestDTO;
import com.worker.dto.ResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.paukov.combinatorics3.Generator;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkerService {
    private static final Logger logger = LogManager.getLogger(WorkerService.class);
    public ResponseDTO crackHash(RequestDTO requestDTO) {
        logger.info("Processing crackHash request for requestId: {}", requestDTO.getRequestId());
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

        return response;
    }
}