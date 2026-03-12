package com.kinforgework.cplaneta.dto;

import com.kinforgework.cplaneta.enums.DeliveryStatus;

import java.time.OffsetDateTime;

public record MessageLogResponse(
        Long id,
        Long contactId,
        String contactEmail,
        Long masterProgramId,
        String masterProgramName,
        DeliveryStatus emailStatus,
        DeliveryStatus whatsappStatus,
        OffsetDateTime emailSentAt,
        OffsetDateTime whatsappSentAt,
        String errorMessage,
        OffsetDateTime createdAt
) {
}
