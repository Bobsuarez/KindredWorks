package com.kinforgework.cplaneta.mapper;

import com.kinforgework.cplaneta.dto.MessageLogResponse;
import com.kinforgework.cplaneta.entities.MessageLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageLogMapper {

    @Mapping(source = "contact.id", target = "contactId")
    @Mapping(source = "contact.email", target = "contactEmail")
    @Mapping(source = "masterProgram.id", target = "masterProgramId")
    @Mapping(source = "masterProgram.name", target = "masterProgramName")
    MessageLogResponse toResponse(MessageLogEntity log);
}
