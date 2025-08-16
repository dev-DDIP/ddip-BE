package com.knu.ddip.location.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "LOCATIONS")
public class LocationEntity {

    @Id
    private String cellId;

    public static LocationEntity create(String cellId) {
        return LocationEntity.builder()
                .cellId(cellId)
                .build();
    }
}
