package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.entities.ImportJobEntity;
import com.kinforgework.cplaneta.repository.ImportJobRepository;
import com.kinforgework.cplaneta.utils.RedisPublisherUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository repository;
    private final RedisPublisherUtil redisPublisher;
    private final WorkerLauncherServices workerLauncherServices;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ImportJobEntity crearJob(MultipartFile archivo) throws IOException {

        // 1. Validar extensión
        String originalName = archivo.getOriginalFilename();
        if (originalName == null ||
                (!originalName.endsWith(".csv") && !originalName.endsWith(".xlsx"))) {
            throw new IllegalArgumentException("Solo se permiten archivos CSV o XLSX");
        }

        // 2. Preparar ruta y guardar archivo
        String filename = UUID.randomUUID() + "_" + originalName;
        Path rootPath = Path.of(uploadDir).toAbsolutePath().normalize();
        Path destino = rootPath.resolve(filename);

        log.info("Iniciando carga de archivo de importación. Nombre: {}, Destino: {}", filename, destino);

        Files.createDirectories(destino.getParent());

        try (var inputStream = archivo.getInputStream()) {
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Archivo guardado exitosamente en: {}", destino);

        // 3. Crear registro en DB
        ImportJobEntity job = new ImportJobEntity();
        job.setFilename(filename);
        job.setOriginalName(originalName);
        repository.save(job);

        // 4. Publicar a Redis para que Python lo consuma
        redisPublisher.publisherJob(job.getId(), filename);
        workerLauncherServices.invoke();   // ← levanta el worker si no está corriendo


        return job;
    }

    public ImportJobEntity obtenerJob(UUID jobId) {
        return repository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job no encontrado: " + jobId));
    }

    @Transactional(readOnly = true)
    public Page<ImportJobEntity> findAll(String search, Pageable pageable) {
        return repository.findByOriginalNameContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                search, search, pageable);
    }
}
