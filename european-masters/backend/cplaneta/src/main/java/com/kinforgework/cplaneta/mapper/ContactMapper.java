package com.kinforgework.cplaneta.mapper;

import com.kinforgework.cplaneta.dto.ContactRequest;
import com.kinforgework.cplaneta.dto.ContactResponse;
import com.kinforgework.cplaneta.entities.ContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    @Mapping(source = "masterProgram.id", target = "masterProgramId")
    @Mapping(source = "masterProgram.name", target = "masterProgramName")
    ContactResponse toResponse(ContactEntity contactEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "masterProgram", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ContactEntity toEntity(ContactRequest request);
}
