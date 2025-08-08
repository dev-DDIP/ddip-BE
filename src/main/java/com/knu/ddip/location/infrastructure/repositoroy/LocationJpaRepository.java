package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationJpaRepository extends JpaRepository<LocationEntity, String> {
}
