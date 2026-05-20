package com.bolota.wiresentinelserver.Resource;

import com.bolota.wiresentinelserver.Entities.SystemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SystemEntityRepository extends JpaRepository<SystemEntity, Long> {

    boolean existsByUuid(UUID UUID);

    boolean existsByMockName(String mockName);
    Page<SystemEntity> findByUuid(UUID uuid, Pageable pageable);

    SystemEntity getByUuid(UUID uuid);
}
