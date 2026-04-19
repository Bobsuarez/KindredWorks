package com.kinforgework.cplaneta.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Getter
@Setter
public class ImportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String filename;
    private String originalName;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    private Integer totalRows;
    private Integer processed = 0;
    private Integer errors = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
