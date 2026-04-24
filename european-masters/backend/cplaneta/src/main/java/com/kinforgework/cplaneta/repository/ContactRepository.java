package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.enums.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

    /**
     * Retrieves the next pending contactEntity ordered by masterProgramId ASC.
     * Uses Pageable(0,1) to fetch exactly one row efficiently.
     */

    @Query(value = """
            SELECT c.*
            FROM contacts c
            JOIN master_programs mp ON c.master_program_id = mp.id
            WHERE c.status::text = :status
            ORDER BY c.id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ContactEntity> findFirstByStatus(@Param("status") String status);

    /**
     * Counts how many contacts transitioned to SENT or ERROR today,
     * used to enforce the daily dispatch limit.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM message_logs ml
            WHERE ml.created_at >= :startOfDay
              AND ml.created_at < :endOfDay
            """, nativeQuery = true)
    long countDispatchedToday(
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

    long countByStatus(ContactStatus status);

    List<ContactEntity> findByMasterProgramId(Long masterProgramId);

    boolean existsByEmailAndMasterProgramId(String email, Long masterProgramId);
}
