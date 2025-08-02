package com.knu.ddip.user.infrastructure.entity;

import com.knu.ddip.user.business.dto.UserEntityDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "USERS")
public class UserEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(unique = true)
    private String email;

    private String nickName;

    @Enumerated(EnumType.STRING)
    private UserEntityStatus status;

    public static UserEntity create(String email, String nickName, String status) {
        return UserEntity.builder()
                .email(email)
                .nickName(nickName)
                .status(UserEntityStatus.valueOf(status))
                .build();
    }

    public static UserEntity create(UUID id, String email, String nickName, String status) {
        return UserEntity.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .status(UserEntityStatus.valueOf(status))
                .build();
    }

    public void update(UserEntityDto userEntityDto) {
        this.email = userEntityDto.getEmail();
        this.nickName = userEntityDto.getNickname();
        this.status = UserEntityStatus.valueOf(userEntityDto.getStatus());
    }

    public UserEntityDto toEntityDto() {
        return UserEntityDto.create(id, email, nickName, status.name());
    }
}
