package com.kinforgework.cplaneta.dto;

import com.kinforgework.cplaneta.enums.NotificationChannel;

import java.time.OffsetDateTime;

public record NotificationChannelStatusResponse(
        Long masterProgramId,
        String masterProgramName,
        Long contactsCount,
        NotificationChannel channel,
        Boolean isEnabled,
        OffsetDateTime effectiveFrom,
        OffsetDateTime effectiveTo,
        OffsetDateTime updatedAt,
        String reason
) {
}
