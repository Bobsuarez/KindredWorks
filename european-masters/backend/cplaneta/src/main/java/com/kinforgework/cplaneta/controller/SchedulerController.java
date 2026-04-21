package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.service.MessageSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scheduler")
public class SchedulerController {

    private final MessageSchedulerService messageScheduler;

    @PostMapping("/start")
    public ResponseEntity<String> restart() {
        messageScheduler.start();
        return ResponseEntity.ok("Scheduler reiniciado correctamente.");
    }
}
