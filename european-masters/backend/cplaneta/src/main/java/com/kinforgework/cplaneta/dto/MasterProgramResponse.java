package com.kinforgework.cplaneta.dto;

import java.time.OffsetDateTime;

public record MasterProgramResponse(
        Long id,
        String name,
        Long areaId,
        String areaName,
        String pdfCurriculumPath,
        String subjectImagePath,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
