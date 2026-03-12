package com.kinforgework.cplaneta.repository;

import com.kinforgework.cplaneta.entities.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, Long> {

    Optional<AreaEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
