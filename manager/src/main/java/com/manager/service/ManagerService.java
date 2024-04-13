package com.manager.service;

import com.manager.dto.CrackRequestDTO;
import com.manager.dto.CrackResponseDTO;
import com.manager.dto.HashDTO;
import com.manager.dto.StatusResponseDTO;
import com.manager.enums.TaskStatus;
import com.manager.exception.NotMD5Hash;
import com.manager.exception.NoSuchTask;
import com.manager.exception.RabbitException;
import com.manager.model.ActiveTaskDocument;
import com.manager.model.ReadyTasksDocument;
import com.manager.producer.ManagerProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Transactional
    public String crackHash(HashDTO hashDTO) throws NotMD5Hash, RabbitException {
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

            ActiveTaskDocument activeTaskDocument = new ActiveTaskDocument();
            activeTaskDocument.setRequestId(requestId);
            activeTaskDocument.setHash(crackRequestDTO.getHash());
            activeTaskDocument.setMaxLength(crackRequestDTO.getMaxLength());
            activeTaskDocument.setAlphabet(crackRequestDTO.getAlphabet());
            activeTaskDocument.setPartNumber(crackRequestDTO.getPartNumber());
            activeTaskDocument.setPartAlphabet(crackRequestDTO.getPartAlphabet());
            activeTaskDocument.setStatus(TaskStatus.IN_PROGRESS);

            mongoTemplate.insert(activeTaskDocument, "activeTasks");

            managerProducer.sendRequest(crackRequestDTO);

            startIndex = endIndex;
        }

        return requestId;
    }

    @Transactional
    public void processWorkerResponse(CrackResponseDTO crackResponseDTO) {
        logger.info("Got response from worker with task {}", crackResponseDTO.getRequestId());
        logger.info("results: {}", crackResponseDTO.getAnswers());

        ReadyTasksDocument readyTasksDocument = new ReadyTasksDocument();
        readyTasksDocument.setRequestId(crackResponseDTO.getRequestId());
        readyTasksDocument.setPartNumber(crackResponseDTO.getPartNumber());
        readyTasksDocument.setAnswers(crackResponseDTO.getAnswers());
        readyTasksDocument.setStatus(TaskStatus.READY);

        mongoTemplate.save(readyTasksDocument, "readyTasks");

        boolean allPartsReady = areAllPartsReady(crackResponseDTO.getRequestId());
        if (allPartsReady) {
            Query query = Query.query(Criteria.where("requestId").is(crackResponseDTO.getRequestId()));
            mongoTemplate.remove(query, "activeTasks");
        }
    }

    private boolean areAllPartsReady(String requestId) {
        Query query = Query.query(Criteria.where("requestId").is(requestId));
        long totalParts = mongoTemplate.count(query, "activeTasks");
        long readyParts = mongoTemplate.count(Query.query(Criteria.where("requestId").is(requestId).and("status").is(TaskStatus.READY)), "readyTasks");
        return readyParts == totalParts;
    }

    @Transactional
    public StatusResponseDTO checkStatus(String requestId) throws NoSuchTask {
        ActiveTaskDocument activeTask = mongoTemplate.findOne(Query.query(Criteria.where("requestId").is(requestId)), ActiveTaskDocument.class, "activeTasks");

        if (activeTask != null) {
            return new StatusResponseDTO(TaskStatus.IN_PROGRESS, null);
        }

        List<ReadyTasksDocument> readyTasks = mongoTemplate.find(Query.query(Criteria.where("requestId").is(requestId).and("answers").exists(true)), ReadyTasksDocument.class, "readyTasks");
        if (!readyTasks.isEmpty()) {
            List<String> decryptedWords = new ArrayList<>();
            for (ReadyTasksDocument readyTask : readyTasks) {
                decryptedWords.addAll(readyTask.getAnswers());
            }
            return new StatusResponseDTO(TaskStatus.READY, decryptedWords);
        }

        throw new NoSuchTask(String.format("Task with UUID %s is not found", requestId));
    }
}