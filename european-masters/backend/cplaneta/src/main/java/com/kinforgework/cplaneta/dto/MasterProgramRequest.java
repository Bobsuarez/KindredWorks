package com.kinforgework.cplaneta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MasterProgramRequest(

        @NotBlank(message = "Program name is required")
        @Size(max = 300)
        String name,

        @NotNull(message = "Area masterProgramId is required")
        Long areaId
) {
}
