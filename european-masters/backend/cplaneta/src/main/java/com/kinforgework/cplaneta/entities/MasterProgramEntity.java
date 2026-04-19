package com.kinforgework.cplaneta.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "master_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"area", "contacts"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MasterProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    /**
     * Filesystem path or NFS path to the PDF curriculum.
     * Example: /uploads/2024/06/1/curriculum.pdf
     */
    @Column(name = "pdf_curriculum_path", nullable = false, length = 512)
    private String pdfCurriculumPath;

    /**
     * Filesystem path or NFS path to the subject image.
     * Example: /uploads/2024/06/1/subject-image.png
     */
    @Column(name = "subject_image_path", nullable = false, length = 512)
    private String subjectImagePath;

    @OneToMany(mappedBy = "masterProgram", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ContactEntity> contactEntities;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
