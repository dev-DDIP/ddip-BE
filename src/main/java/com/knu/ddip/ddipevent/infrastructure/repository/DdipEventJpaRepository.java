package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DdipEventJpaRepository extends JpaRepository<DdipEventEntity, UUID> {
    @Query(value = """
                SELECT * FROM ddip_event
                WHERE ST_CONTAINS(ST_Buffer(ST_SRID(POINT(:lng, :lat), 4326), :dist), local_point)
                ORDER BY ST_Distance_Sphere(ST_SRID(POINT(:lng, :lat), 4326), local_point)
            """, nativeQuery = true)
    List<DdipEventEntity> findAllByDistance(@Param("lng") Double lng, @Param("lat") Double lat, @Param("dist") Double dist);
}
