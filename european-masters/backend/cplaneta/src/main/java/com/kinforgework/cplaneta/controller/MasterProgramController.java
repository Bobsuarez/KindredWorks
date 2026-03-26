package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.dto.MasterProgramRequest;
import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.service.MasterProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/degrees")
@RequiredArgsConstructor
public class MasterProgramController {

    private final MasterProgramService masterProgramService;

    @GetMapping
    public ResponseEntity<Page<MasterProgramResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(masterProgramService.findAll(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MasterProgramResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(masterProgramService.findById(id));
    }

    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<List<MasterProgramResponse>> findByArea(@PathVariable Long areaId) {
        return ResponseEntity.ok(masterProgramService.findByArea(areaId));
    }

    /**
     * Retrieves the curriculum PDF for a specific master program.
     */
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) throws IOException {
        byte[] pdf = masterProgramService.getCurriculumPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=curriculum-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Multipart endpoint to create a new program.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MasterProgramResponse> create(
            @RequestPart("data") @Valid MasterProgramRequest request,
            @RequestPart("curriculum") MultipartFile curriculum,
            @RequestPart(value = "subjectImage", required = false) MultipartFile subjectImage
    ) throws IOException {

        MasterProgramResponse response = masterProgramService.create(request, curriculum, subjectImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Multipart endpoint to update an existing program. Files are optional.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MasterProgramResponse> update(
            @PathVariable Long id,
            @RequestPart("data") @Valid MasterProgramRequest request,
            @RequestPart(value = "curriculum", required = false) MultipartFile curriculum,
            @RequestPart(value = "subjectImage", required = false) MultipartFile subjectImage
    ) throws IOException {

        MasterProgramResponse response = masterProgramService.update(id, request, curriculum, subjectImage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        masterProgramService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
