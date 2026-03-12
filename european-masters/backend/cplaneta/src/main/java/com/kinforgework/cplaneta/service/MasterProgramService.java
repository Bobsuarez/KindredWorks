package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.dto.MasterProgramRequest;
import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.entities.AreaEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.exception.ResourceNotFoundException;
import com.kinforgework.cplaneta.mapper.MasterProgramMapper;
import com.kinforgework.cplaneta.repository.MasterProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterProgramService {

    private final MasterProgramRepository masterProgramRepository;
    private final MasterProgramMapper masterProgramMapper;
    private final AreaService areaService;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<MasterProgramResponse> findAll() {
        return masterProgramRepository.findAll()
                .stream()
                .map(masterProgramMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MasterProgramResponse findById(Long id) {
        return masterProgramMapper.toResponse(getProgramOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MasterProgramResponse> findByArea(Long areaId) {
        return masterProgramRepository.findByAreaIdWithArea(areaId)
                .stream()
                .map(masterProgramMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new MasterProgram, persisting uploaded files first.
     * The entity is saved only after both files are stored successfully,
     * preserving consistency between the DB record and the filesystem.
     */
    @Transactional
    public MasterProgramResponse create(
            MasterProgramRequest request,
            MultipartFile curriculum,
            MultipartFile subjectImage
    ) throws IOException {

        AreaEntity areaEntity = areaService.getAreaOrThrow(request.areaId());

        // Persist with placeholder paths first to obtain the generated id
        MasterProgramEntity program = MasterProgramEntity.builder()
                .name(request.name())
                .area(areaEntity)
                .pdfCurriculumPath("PENDING")
                .subjectImagePath("PENDING")
                .build();

        MasterProgramEntity saved = masterProgramRepository.save(program);

        // Store files using the generated id as directory segment
        String pdfPath = fileStorageService.storeCurriculum(curriculum, saved.getName());
        String imagePath = fileStorageService.storeSubjectImage(subjectImage, saved.getName());

        saved.setPdfCurriculumPath(pdfPath);
        saved.setSubjectImagePath(imagePath);
        masterProgramRepository.save(saved);

        log.info("MasterProgram created: id={} name='{}'", saved.getId(), saved.getName());
        return masterProgramMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        MasterProgramEntity program = getProgramOrThrow(id);
        masterProgramRepository.delete(program);
        log.info("MasterProgram deleted: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Package-private helper
    // -------------------------------------------------------------------------

    MasterProgramEntity getProgramOrThrow(Long id) {
        return masterProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterProgram not found: " + id));
    }
}
