package com.kinforgework.cplaneta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Contact name is required")
        @Size(max = 200)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 320)
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(max = 30)
        String phoneNumber,

        @NotNull(message = "Master program id is required")
        Long masterProgramId
) {
}
