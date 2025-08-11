package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationJpaRepository extends JpaRepository<LocationEntity, String> {
    List<LocationEntity> findAllByCellIdIn(List<String> cellIds);
}
