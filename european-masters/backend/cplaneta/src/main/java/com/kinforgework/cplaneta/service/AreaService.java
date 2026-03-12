package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.dto.AreaRequest;
import com.kinforgework.cplaneta.dto.AreaResponse;
import com.kinforgework.cplaneta.entities.AreaEntity;
import com.kinforgework.cplaneta.exception.ResourceAlreadyExistsException;
import com.kinforgework.cplaneta.exception.ResourceNotFoundException;
import com.kinforgework.cplaneta.mapper.AreaMapper;
import com.kinforgework.cplaneta.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {

    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;

    @Transactional(readOnly = true)
    public List<AreaResponse> findAll() {
        return areaRepository.findAll()
                .stream()
                .map(areaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaResponse findById(Long id) {
        return areaMapper.toResponse(getAreaOrThrow(id));
    }

    @Transactional
    public List<AreaResponse> create(List<AreaRequest> request) {

        return request.stream()
                .peek(data -> {
                    if (areaRepository.existsByNameIgnoreCase(data.name())) {
                        throw new ResourceAlreadyExistsException("Area already exists: " + data.name());
                    }
                })
                .map(data -> {
                    AreaEntity areaEntity = areaMapper.toEntity(data);
                    AreaEntity saved = areaRepository.save(areaEntity);
                    log.info("Area created: id={} name='{}'", saved.getId(), saved.getName());
                    return areaMapper.toResponse(saved);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AreaResponse update(Long id, AreaRequest request) {
        AreaEntity areaEntity = getAreaOrThrow(id);
        areaEntity.setName(request.name());
        AreaEntity saved = areaRepository.save(areaEntity);
        log.info("Area updated: id={} name='{}'", saved.getId(), saved.getName());
        return areaMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        AreaEntity areaEntity = getAreaOrThrow(id);
        areaRepository.delete(areaEntity);
        log.info("Area deleted: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Package-private helper used by other services
    // -------------------------------------------------------------------------

    AreaEntity getAreaOrThrow(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found: " + id));
    }
}
