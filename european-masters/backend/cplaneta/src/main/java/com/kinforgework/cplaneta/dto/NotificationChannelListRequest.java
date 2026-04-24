package com.kinforgework.cplaneta.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationChannelListRequest(

        @NotNull(message = "name is required")
        String name,

        @NotNull(message = "idEnable is required")
        Boolean idEnable
) {
}