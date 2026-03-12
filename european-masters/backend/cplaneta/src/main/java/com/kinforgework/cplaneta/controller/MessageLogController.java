package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.dto.MessageLogResponse;
import com.kinforgework.cplaneta.entities.MessageLogEntity;
import com.kinforgework.cplaneta.mapper.MessageLogMapper;
import com.kinforgework.cplaneta.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/message-logs")
@RequiredArgsConstructor
public class MessageLogController {

    private final MessageLogRepository messageLogRepository;
    private final MessageLogMapper messageLogMapper;

    @GetMapping
    public ResponseEntity<List<MessageLogResponse>> findAll() {
        List<MessageLogEntity> logs = messageLogRepository.findAll();
        return ResponseEntity.ok(logs.stream()
                                         .map(messageLogMapper::toResponse)
                                         .toList());
    }

    @GetMapping("/by-contact/{contactId}")
    public ResponseEntity<List<MessageLogResponse>> findByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(
                messageLogRepository.findByContactId(contactId)
                        .stream()
                        .map(messageLogMapper::toResponse)
                        .toList());
    }

    @GetMapping("/by-program/{programId}")
    public ResponseEntity<List<MessageLogResponse>> findByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(
                messageLogRepository.findByMasterProgramId(programId)
                        .stream()
                        .map(messageLogMapper::toResponse)
                        .toList());
    }
}
