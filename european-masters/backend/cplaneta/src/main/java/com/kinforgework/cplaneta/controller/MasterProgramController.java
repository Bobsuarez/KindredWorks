package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.dto.MasterProgramRequest;
import com.kinforgework.cplaneta.dto.MasterProgramResponse;
import com.kinforgework.cplaneta.service.MasterProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class MasterProgramController {

    private final MasterProgramService masterProgramService;

    @GetMapping
    public ResponseEntity<List<MasterProgramResponse>> findAll() {
        return ResponseEntity.ok(masterProgramService.findAll());
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
     * Multipart endpoint: accepts JSON metadata + two file parts.
     * <p>
     * Example cURL:
     * <pre>
     *   curl -X POST /api/v1/programs \
     *     -F 'data={"name":"Maestría en Marketing","areaId":2};type=application/json' \
     *     -F 'curriculum=@/path/to/curriculum.pdf' \
     *     -F 'subjectImage=@/path/to/image.png'
     * </pre>
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MasterProgramResponse> create(
            @RequestPart("data") @Valid MasterProgramRequest request,
            @RequestPart("curriculum") MultipartFile curriculum,
            @RequestPart("subjectImage") MultipartFile subjectImage
    ) throws IOException {

        MasterProgramResponse response = masterProgramService.create(request, curriculum, subjectImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        masterProgramService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
