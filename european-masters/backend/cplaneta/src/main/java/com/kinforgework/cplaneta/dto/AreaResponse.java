package com.kinforgework.cplaneta.dto;

import java.time.OffsetDateTime;

public record AreaResponse(
        Long id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
