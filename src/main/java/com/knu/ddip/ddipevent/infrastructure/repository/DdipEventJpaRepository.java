package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DdipEventJpaRepository extends JpaRepository<DdipEventEntity, UUID> {
    List<DdipEventEntity> findAllByCellIdIn(List<String> cellIds);
}
