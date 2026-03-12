package com.kinforgework.cplaneta.dto;

import com.kinforgework.cplaneta.enums.ContactStatus;

import java.time.OffsetDateTime;

public record ContactResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        Long masterProgramId,
        String masterProgramName,
        ContactStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
