package com.knu.ddip.auth.infrastructure.repository;

import com.knu.ddip.auth.domain.OAuthProvider;
import com.knu.ddip.auth.infrastructure.entity.OAuthMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthMappingJpaRepository extends JpaRepository<OAuthMappingEntity, UUID> {

    Optional<OAuthMappingEntity> findBySocialUserIdAndProvider(String socialUserId,
                                                               OAuthProvider provider);

    Optional<OAuthMappingEntity> findByIdAndProvider(UUID id, OAuthProvider provider);
}
