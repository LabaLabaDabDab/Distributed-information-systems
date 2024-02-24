package com.manager.service;


import com.manager.dto.CrackRequestDTO;
import com.manager.dto.CrackResponseDTO;
import com.manager.dto.HashDTO;
import com.manager.dto.StatusResponseDTO;
import com.manager.enums.TaskStatus;
import com.manager.exception.NoSuchTask;
import com.manager.exception.NotMD5Hash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ManagerService {
    private static final Logger logger = LogManager.getLogger(ManagerService.class);
    private static final int TIMEOUT = 40;
    private static final String WORKER_CRACK_ENDPOINT = "http://worker:8081/internal/api/worker/hash/crack/task";
    private final ConcurrentHashMap<String, StatusResponseDTO> taskStatuses = new ConcurrentHashMap<>();
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

        String requestId = UUID.nameUUIDFromBytes(hashDTO.getHash().getBytes(StandardCharsets.UTF_8)).toString();

        if (taskStatuses.containsKey(requestId) && taskStatuses.get(requestId).getStatus() != TaskStatus.ERROR) {
            logger.info("Task with UUID {} is already executing and its status is not ERROR", requestId);
            return requestId;
        }

        CrackRequestDTO crackRequestDTO = new CrackRequestDTO();
        crackRequestDTO.setHash(hashDTO.getHash());
        crackRequestDTO.setMaxLength(hashDTO.getMaxLength());
        crackRequestDTO.setRequestId(requestId);
        crackRequestDTO.setAlphabet(alphabet);

        WebClient client = WebClient.create(WORKER_CRACK_ENDPOINT);
        client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(crackRequestDTO)
                .retrieve()
                .bodyToMono(CrackResponseDTO.class)
                .timeout(Duration.ofSeconds(TIMEOUT))
                .subscribe(
                        response -> {
                            logger.info("Got response from worker with task {}", requestId);
                            logger.info("{} results:", requestId);
                            logger.info(response.getAnswers());

                            StatusResponseDTO statusResponseDTO = new StatusResponseDTO();
                            statusResponseDTO.setStatus(TaskStatus.READY);
                            statusResponseDTO.setData(response.getAnswers());
                            taskStatuses.put(requestId, statusResponseDTO);
                        },
                        error -> {
                            logger.error("Error receiving response from worker {}", requestId);
                            logger.error(error);

                            StatusResponseDTO statusResponseDTO = new StatusResponseDTO();
                            statusResponseDTO.setStatus(TaskStatus.ERROR);
                            taskStatuses.put(requestId, statusResponseDTO);
                        }
                );

        StatusResponseDTO initialStatus = new StatusResponseDTO();
        initialStatus.setStatus(TaskStatus.IN_PROGRESS);
        taskStatuses.put(requestId, initialStatus);

        return requestId;
    }

    public StatusResponseDTO checkStatus(String requestId) throws NoSuchTask {
        if (!taskStatuses.containsKey(requestId)) {
            throw new NoSuchTask(String.format("Task with UUID %s is not found", requestId));
        }

        return taskStatuses.get(requestId);
    }
}