package com.manager.service;

import com.manager.dto.CrackRequestDTO;
import com.manager.dto.CrackResponseDTO;
import com.manager.dto.HashDTO;
import com.manager.exception.NotMD5Hash;
import com.manager.exception.RabbitException;
import com.manager.producer.ManagerProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ManagerService {
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final Logger logger = LogManager.getLogger(ManagerService.class);
    //private static final String WORKER_CRACK_ENDPOINT = "http://localhost:8081/internal/api/worker/hash/crack/task";
    private final ManagerProducer managerProducer;
    private  static final Integer workersCount = 2;

    public ManagerService(ManagerProducer managerProducer) {
        this.managerProducer = managerProducer;

    }

    private final List<String> alphabet = Arrays.asList(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z"
    );

    private boolean isMD5(String hash) {
        String pattern = "^[a-fA-F\\d]{32}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(hash);
        return matcher.matches();
    }

    public String crackHash(HashDTO hashDTO) throws NotMD5Hash {
        if (!isMD5(hashDTO.getHash())) {
            throw new NotMD5Hash(String.format("%s is not a valid MD5 hash", hashDTO.getHash()));
        }

        if (hashDTO.getMaxLength() < 1) {
            throw new IllegalArgumentException(String.format("%d is not a valid maximum length for the target word", hashDTO.getMaxLength()));
        }

        String requestId = UUID.randomUUID().toString();

        int alphabetSize = alphabet.size();
        int lettersPerWorker = alphabetSize / workersCount;
        int remainder = alphabetSize % workersCount;
        int startIndex = 0;

        for (int i = 0; i < workersCount; i++){
            int endIndex = startIndex + lettersPerWorker;
            if (i < remainder) {
                endIndex++;
            }

            List<String> partAlphabet = alphabet.subList(startIndex, endIndex);
            CrackRequestDTO crackRequestDTO = new CrackRequestDTO();
            crackRequestDTO.setHash(hashDTO.getHash());
            crackRequestDTO.setMaxLength(hashDTO.getMaxLength());
            crackRequestDTO.setRequestId(requestId);
            crackRequestDTO.setAlphabet(alphabet);
            crackRequestDTO.setPartNumber(i);
            crackRequestDTO.setPartAlphabet(partAlphabet);

            try {
                managerProducer.sendRequest(crackRequestDTO);
            }
            catch (RabbitException e){
                mongoTemplate.insert(crackRequestDTO);
            }

            startIndex = endIndex;
        }

        /*StatusResponseDTO initialStatus = new StatusResponseDTO();
        initialStatus.setStatus(TaskStatus.IN_PROGRESS);
        taskStatuses.put(requestId, initialStatus);*/

        return requestId;
    }

    public void processWorkerResponse(CrackResponseDTO crackResponseDTO) {
        logger.info("Got response from worker with task {}", crackResponseDTO.getRequestId());
        logger.info("{} results:", crackResponseDTO.getAnswers());
        logger.info(crackResponseDTO.getAnswers());


    }



    /*private void updateTaskStatus(String requestId, TaskStatus newStatus) {
        taskStatuses.computeIfPresent(requestId, (key, existingStatus) -> {
            existingStatus.setStatus(newStatus);
            return existingStatus;
        });
    }

    public StatusResponseDTO checkStatus(String requestId) throws NoSuchTask {
        if (!taskStatuses.containsKey(requestId)) {
            throw new NoSuchTask(String.format("Task with UUID %s is not found", requestId));
        }

        return taskStatuses.get(requestId);
    }*/
}