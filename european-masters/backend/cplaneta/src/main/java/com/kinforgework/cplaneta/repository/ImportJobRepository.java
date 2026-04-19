package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.ImportJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJobEntity, UUID> {

    Page<ImportJobEntity> findByOriginalNameContainingIgnoreCaseOrFilenameContainingIgnoreCase(
            String originalName, String filename, Pageable pageable);
}
