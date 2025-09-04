package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DdipEventJpaRepository extends JpaRepository<DdipEventEntity, UUID> {
}
