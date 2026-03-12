package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.scheduler.MessageScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final MessageScheduler messageScheduler;

    @PostMapping("/restart")
    public ResponseEntity<String> restart() {
        messageScheduler.restart();
        return ResponseEntity.ok("Scheduler reiniciado correctamente.");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        messageScheduler.stop();
        return ResponseEntity.ok("Scheduler detenido correctamente.");
    }
}
