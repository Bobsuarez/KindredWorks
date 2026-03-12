package com.kinforgework.cplaneta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaRequest(
        @NotBlank(message = "Area name is required")
        @Size(max = 150)
        String name
) {
}
