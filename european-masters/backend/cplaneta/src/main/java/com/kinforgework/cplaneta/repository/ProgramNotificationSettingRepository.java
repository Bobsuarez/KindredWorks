package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.ProgramNotificationSettingEntity;
import com.kinforgework.cplaneta.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProgramNotificationSettingRepository extends JpaRepository<ProgramNotificationSettingEntity, Long> {

    @Query("""
            SELECT pns
            FROM ProgramNotificationSettingEntity pns
            WHERE pns.masterProgram.id = :masterProgramId
              AND pns.channel = :channel
              AND pns.effectiveTo IS NULL
            """)
    Optional<ProgramNotificationSettingEntity> findActiveByProgramAndChannel(
            @Param("masterProgramId") Long masterProgramId,
            @Param("channel") NotificationChannel channel
    );

    @Query("""
            SELECT pns
            FROM ProgramNotificationSettingEntity pns
            WHERE pns.masterProgram.id = :masterProgramId
              AND pns.effectiveTo IS NULL
            """)
    List<ProgramNotificationSettingEntity> findActiveByProgram(@Param("masterProgramId") Long masterProgramId);

    @Query("""
            SELECT pns
            FROM ProgramNotificationSettingEntity pns
            WHERE pns.effectiveTo IS NULL
            ORDER BY pns.masterProgram.id ASC, pns.channel ASC
            """)
    List<ProgramNotificationSettingEntity> findAllActive();

    @Query("""
            SELECT pns
            FROM ProgramNotificationSettingEntity pns
            WHERE pns.masterProgram.id IN :programIds
              AND pns.effectiveTo IS NULL
            """)
    List<ProgramNotificationSettingEntity> findAllActiveByMasterProgramIn(@Param("programIds") List<Long> programIds);
}
