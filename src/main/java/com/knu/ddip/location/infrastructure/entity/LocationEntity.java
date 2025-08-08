package com.knu.ddip.location.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "LOCATIONS")
public class LocationEntity implements Persistable<String> {

    @Id
    private String cellId;

    public static LocationEntity create(String cellId) {
        return LocationEntity.builder()
                .cellId(cellId)
                .build();
    }

    @Override
    public String getId() {
        return cellId;
    }

    @Override
    public boolean isNew() {
        return cellId == null;
    }
}
