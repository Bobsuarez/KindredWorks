package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.MessageLogEntity;
import com.kinforgework.cplaneta.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLogEntity, Long> {

    List<MessageLogEntity> findByContactId(Long contactId);

    List<MessageLogEntity> findByMasterProgramId(Long masterProgramId);

    @Query(value = """
            SELECT COUNT(*)
            FROM message_logs ml
            WHERE ml.created_at >= :from
              AND ml.created_at < :to
            """, nativeQuery = true)
    long countByCreatedAtBetween(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM message_logs ml
            WHERE ml.created_at >= :from
              AND ml.created_at < :to
              AND ml.email_status = :emailStatus
            """, nativeQuery = true)
    long countByCreatedAtBetweenAndEmailStatus(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("emailStatus") DeliveryStatus emailStatus
    );
}
