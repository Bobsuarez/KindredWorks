package com.kinforgework.cplaneta.mapper;

import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MasterProgramMapper {

    @Mapping(source = "area.id", target = "areaId")
    @Mapping(source = "area.name", target = "areaName")
    MasterProgramResponse toResponse(MasterProgramEntity program);
}
