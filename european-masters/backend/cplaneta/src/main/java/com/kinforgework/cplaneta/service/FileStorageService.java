package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

/**
 * Manages file storage for program assets (PDF curricula, subject images).
 *
 * <p>Storage layout:
 * <pre>
 * {base-path}/{year}/{month}/{programId}/curriculum.pdf
 * {base-path}/{year}/{month}/{programId}/subject-image.{ext}
 * </pre>
 *
 * <p>For NFS deployments, set {@code app.file-storage.base-path} to the
 * NFS mount point. The service is otherwise transparent to the caller.
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file-storage.base-path.upload}")
    private String basePathUploads;

    @Value("${app.file-storage.base-path.files-excel}")
    private String basePathFilesExcel;

    private Path baseDirUploads;
    private Path baseDirFilesExcel;

    @PostConstruct
    void init() throws IOException {
        this.baseDirUploads = resolveBaseDir(basePathUploads);
        this.baseDirFilesExcel = resolveBaseDir(basePathFilesExcel);
        buildToCreateFiles(baseDirUploads);
        buildToCreateFiles(baseDirFilesExcel);
    }

    private void buildToCreateFiles(Path baseDir) {
        try {
            Files.createDirectories(baseDir);
            if (!Files.isDirectory(baseDir) || !Files.isWritable(baseDir)) {
                throw new IllegalStateException("File storage base directory is not writable: " + baseDir);
            }
            log.info(
                    "File storage base directory: '{}'", baseDir.toAbsolutePath()
                            .normalize()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Write operations (used when registering a new program)
    // -------------------------------------------------------------------------

    /**
     * Stores the uploaded PDF curriculum and returns its filesystem path.
     */
    public String storeCurriculum(MultipartFile file, String programName) throws IOException {
        Path targetDir = resolveDirectory(programName);
        Path targetPath = targetDir.resolve("curriculum.pdf");
        file.transferTo(targetPath.toFile());
        log.info("Curriculum stored at '{}'", targetPath);
        return targetPath.toString();
    }

    /**
     * Stores the uploaded subject image and returns its filesystem path.
     */
    public String storeSubjectImage(MultipartFile file, String programName) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        Path targetDir = resolveDirectory(programName);
        Path targetPath = targetDir.resolve("subject-image." + extension);
        file.transferTo(targetPath.toFile());
        log.info("Subject image stored at '{}'", targetPath);
        return targetPath.toString();
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Reads the file at the given path into a byte array.
     */
    public byte[] readFileBytes(String storedPath) throws IOException {
        Path path = Paths.get(storedPath);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File not found at: " + storedPath);
        }
        return Files.readAllBytes(path);
    }
    // -------------------------------------------------------------------------
    // Delete operations
    // -------------------------------------------------------------------------

    /**
     * Deletes the file if it exists.
     */
    public boolean deleteRecursively(String storedPath){

        return Optional.ofNullable(storedPath)
                .map(Paths::get)
                .filter(Files::exists)
                .map(this::deleteDirectory)
                .orElse(false);
    }

    private boolean deleteDirectory(Path path) {

        try {
            return Files.walk(path)
                    .allMatch(p -> {
                        try {
                            Files.delete(p);
                            return true;
                        } catch (IOException e) {
                            log.error("Failed to delete file: {}", p, e);
                            return false;
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Resolves a stored path string to an existing {@link File}.
     *
     * @throws IllegalStateException if the file does not exist
     */
    public File resolveFile(String storedPath) {
        File file = Paths.get(storedPath).toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException(
                    "Required file not found at path: " + storedPath);
        }
        return file;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Path resolveDirectory(String programName) throws IOException {

        String safeName = programName.toLowerCase()
                .trim()
                .replaceAll("\\s+", "_");

        LocalDate today = LocalDate.now();
        Path dir = Paths.get(
                baseDirUploads.toString(),
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                safeName
        );
        Files.createDirectories(dir);
        return dir;
    }

    private Path resolveBaseDir(String configuredBasePath) {
        try {
            Path p = Paths.get(configuredBasePath);
            if (!p.isAbsolute()) {
                p = Paths.get(System.getProperty("user.dir"))
                        .resolve(p);
            }
            return p.normalize();
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid app.file-storage.base-path: " + configuredBasePath, ex);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();
    }
}
