package com.kinforgework.cplaneta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ProgramChannelUpdate(

        @JsonProperty("masterProgramId")
        @NotNull(message = "masterProgramId is required")
        Long masterProgramId,

        @NotNull(message = "channelListRequest is required")
        Map<String, Boolean> channels  // {"email": true, "whatsapp": false}
) {
}