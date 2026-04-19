package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterProgramRepository extends JpaRepository<MasterProgramEntity, Long> {

    Page<MasterProgramEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
