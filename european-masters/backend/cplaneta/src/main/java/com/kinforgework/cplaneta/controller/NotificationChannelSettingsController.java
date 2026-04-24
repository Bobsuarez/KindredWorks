package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.dto.NotificationChannelStatusResponse;
import com.kinforgework.cplaneta.service.ProgramNotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelSettingsController {

    private final ProgramNotificationSettingService notificationSettingService;

    @GetMapping("/status")
    public ResponseEntity<List<NotificationChannelStatusResponse>> findAllActiveStatuses() {
        return ResponseEntity.ok(notificationSettingService.findAllActiveStatuses());
    }
}
