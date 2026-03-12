package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterProgramRepository extends JpaRepository<MasterProgramEntity, Long> {

    @Query(value = """
            SELECT mp.*
            FROM master_programs mp
            WHERE mp.area_id = :areaId
            """, nativeQuery = true)
    List<MasterProgramEntity> findByAreaIdWithArea(@Param("areaId") Long areaId);

    List<MasterProgramEntity> findByAreaId(Long areaId);
}
