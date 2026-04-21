package com.kinforgework.cplaneta.controller;

import com.kinforgework.cplaneta.entities.ImportJobEntity;
import com.kinforgework.cplaneta.service.ImportJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportJobController {

    private final ImportJobService service;

    @GetMapping
    public ResponseEntity<Page<ImportJobEntity>> findAll(
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
        return ResponseEntity.ok(service.findAll(search, pageable));
    }

    // Recibe el archivo, responde inmediatamente con job_id
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile fileEntry
    ) throws IOException {

        ImportJobEntity job = service.crearJob(fileEntry);

        return ResponseEntity
                .accepted()  // 202 — procesamiento en background
                .body(Map.of(
                        "job_id", job.getId()
                                .toString(),
                        "filename", job.getFilename(),
                        "status", job.getStatus()
                                .name(),
                        "message", "Archivo recibido, procesando en background"
                ));
    }

    // Consulta el estado actual del job (polling o base del SSE)
    @GetMapping("/{jobId}/status")
    public ResponseEntity<ImportJobEntity> status(@PathVariable UUID jobId) {
        return ResponseEntity.ok(service.obtenerJob(jobId));
    }
}
