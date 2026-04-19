package com.kinforgework.cplaneta.mapper;

import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MasterProgramMapper {

    MasterProgramResponse toResponse(MasterProgramEntity program);
}
