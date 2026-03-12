package com.kinforgework.cplaneta.mapper;

import com.kinforgework.cplaneta.dto.AreaRequest;
import com.kinforgework.cplaneta.dto.AreaResponse;
import com.kinforgework.cplaneta.entities.AreaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AreaMapper {

    AreaResponse toResponse(AreaEntity areaEntity);

    AreaEntity toEntity(AreaRequest request);
}
