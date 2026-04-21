package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.dto.MasterProgramRequest;
import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.exception.ResourceNotFoundException;
import com.kinforgework.cplaneta.mapper.MasterProgramMapper;
import com.kinforgework.cplaneta.repository.MasterProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterProgramService {

    private final MasterProgramRepository masterProgramRepository;
    private final MasterProgramMapper masterProgramMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Page<MasterProgramResponse> findAll(String search, Pageable pageable) {
        return masterProgramRepository.findByNameContainingIgnoreCase(search, pageable)
                .map(masterProgramMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MasterProgramResponse findById(Long id) {
        return masterProgramMapper.toResponse(getProgramOrThrow(id));
    }


    @Transactional(readOnly = true)
    public byte[] getCurriculumPdf(Long id) throws IOException {
        MasterProgramEntity program = getProgramOrThrow(id);
        String path = program.getPdfCurriculumPath();

        if (path == null || path.equals("PENDING") || path.isEmpty()) {
            throw new ResourceNotFoundException("PDF path not set for program: " + id);
        }

        return fileStorageService.readFileBytes(path);
    }

    @Transactional(readOnly = true)
    public byte[] getSubjectImage(Long id) throws IOException {
        String path = getSubjectImagePathOrThrow(id);
        return fileStorageService.readFileBytes(path);
    }

    @Transactional(readOnly = true)
    public MediaType getSubjectImageMediaType(Long id) {
        String path = getSubjectImagePathOrThrow(id);
        String detected = URLConnection.guessContentTypeFromName(path);
        if (detected == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.parseMediaType(detected);
    }

    /**
     * Creates a new MasterProgram, persisting uploaded files first.
     */
    @Transactional
    public MasterProgramResponse create(
            MasterProgramRequest request,
            MultipartFile curriculum,
            MultipartFile subjectImage
    ) throws IOException {

        MasterProgramEntity program = MasterProgramEntity.builder()
                .name(request.name())
                .pdfCurriculumPath("PENDING")
                .subjectImagePath("PENDING")
                .build();

        MasterProgramEntity saved = masterProgramRepository.save(program);

        String pdfPath = fileStorageService.storeCurriculum(curriculum, saved.getName());
        String imagePath = isValidMultipartSubject(subjectImage, saved);

        saved.setPdfCurriculumPath(pdfPath);
        saved.setSubjectImagePath(imagePath);
        masterProgramRepository.save(saved);

        log.info("MasterProgram created: id={} name='{}'", saved.getId(), saved.getName());
        return masterProgramMapper.toResponse(saved);
    }

    /**
     * Updates an existing MasterProgram. Files are optional.
     */
    @Transactional
    public MasterProgramResponse update(
            Long id,
            MasterProgramRequest request,
            MultipartFile curriculum,
            MultipartFile subjectImage
    ) throws IOException {

        MasterProgramEntity program = getProgramOrThrow(id);

        program.setName(request.name());

        if (curriculum != null && !curriculum.isEmpty()) {
            String oldPath = program.getPdfCurriculumPath();
            log.info("Old curriculum path: {}", oldPath);
            fileStorageService.deleteRecursively(oldPath);
            String pdfPath = fileStorageService.storeCurriculum(curriculum, program.getName());
            log.info("New curriculum path: {}", pdfPath);
            program.setPdfCurriculumPath(pdfPath);
        }

        if (subjectImage != null && !subjectImage.isEmpty()) {
            String oldPath = program.getSubjectImagePath();
            log.info("Old subject image path: {}", oldPath);
            fileStorageService.deleteRecursively(oldPath);
            String imagePath = fileStorageService.storeSubjectImage(subjectImage, program.getName());
            log.info("New subject image path: {}", imagePath);
            program.setSubjectImagePath(imagePath);
        }

        MasterProgramEntity updated = masterProgramRepository.save(program);
        log.info("MasterProgram updated: id={} name='{}'", updated.getId(), updated.getName());
        return masterProgramMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        MasterProgramEntity program = getProgramOrThrow(id);
        masterProgramRepository.delete(program);
        log.info("MasterProgram deleted: id={}", id);
    }

    private String isValidMultipartSubject(MultipartFile subjectImage, MasterProgramEntity saved) {
        return Optional.ofNullable(subjectImage)
                .map(file -> {
                    try {
                        return fileStorageService.storeSubjectImage(subjectImage, saved.getName());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse("");
    }

    MasterProgramEntity getProgramOrThrow(Long id) {
        return masterProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterProgram not found: " + id));
    }

    private String getSubjectImagePathOrThrow(Long id) {
        MasterProgramEntity program = getProgramOrThrow(id);
        String path = program.getSubjectImagePath();
        if (path == null || path.equals("PENDING") || path.isEmpty()) {
            throw new ResourceNotFoundException("Image path not set for program: " + id);
        }
        return path;
    }
}
